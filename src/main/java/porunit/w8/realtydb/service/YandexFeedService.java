package porunit.w8.realtydb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import porunit.w8.realtydb.data.PublishResponse;
import porunit.w8.realtydb.data.ValidationReport;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

// YandexFeedService.java
@Service
@RequiredArgsConstructor
public class YandexFeedService {

  @Value("${yandex.feed.storageDir}")
  private String storageDir;

  @Value("${yandex.feed.baseUrl}")
  private String baseUrl;

  private static final Set<String> TYPE = Set.of("продажа","аренда","sale","rent"); // допустимые
  private static final Set<String> CATEGORY = Set.of("коммерческая","commercial");
  private static final Set<String> COMMERCIAL_TYPE = Set.of(
      "auto repair","business","free purpose","hotel","land","legal address",
      "manufacturing","office","public catering","retail","warehouse"
  );
  private static final Set<String> BUILDING_TYPE = Set.of(
      "business center","detached building","residential building","shopping center","warehouse"
  );

  public ValidationReport validate(InputStream xml) throws IOException {
    var errors = new ArrayList<ValidationReport.Issue>();
    var warns  = new ArrayList<ValidationReport.Issue>();
    var seenIds = new HashSet<String>();

    try {
      XMLInputFactory f = XMLInputFactory.newFactory();
      XMLStreamReader r = f.createXMLStreamReader(xml);

      String currentOfferId = null;
      boolean inOffer = false;

      boolean hasType=false, hasCategory=false, hasLocation=false, hasPrice=false;

      Deque<String> path = new ArrayDeque<>(); // стек локальных имён

      while (r.hasNext()) {
        int e = r.next();

        if (e == XMLStreamConstants.START_ELEMENT) {
          String name = r.getLocalName();
          path.push(name);

          if ("offer".equals(name)) {
            inOffer = true;
            currentOfferId = getAttr(r, "internal-id");
            if (currentOfferId == null || currentOfferId.isBlank()) {
              errors.add(err("INTERNAL_ID_MISSING", "Отсутствует internal-id у <offer>", "offer"));
            } else if (!seenIds.add(currentOfferId)) {
              errors.add(err("INTERNAL_ID_DUP", "Повтор internal-id: "+currentOfferId, offerPath(currentOfferId)));
            }
            hasType = hasCategory = hasLocation = hasPrice = false;
          }

        } else if (e == XMLStreamConstants.CHARACTERS && inOffer) {
          String text = r.getText().trim();
          if (text.isEmpty()) continue;

          String cur = path.peek();               // текущий элемент
          String parent = path.size() > 1 ? path.toArray(new String[0])[1] : null; // родитель

          // ВАЛИДИРУЕМ ТОЛЬКО /offer/category
          if ("category".equals(cur) && "offer".equals(parent)) {
            hasCategory = true;
            if (!CATEGORY.contains(text)) {
              errors.add(err("CATEGORY_INVALID", "category должен быть 'commercial/коммерческая'",
                      offerPath(currentOfferId)+"/category"));
            }
            continue; // важно!
          }

          // НЕ /offer/category — ничего не делаем (например sales-agent/category)
          // Остальные проверки тоже делаем адресно по пути:
          if ("type".equals(cur) && "offer".equals(parent)) {
            hasType = true;
            if (!TYPE.contains(text)) {
              errors.add(err("TYPE_INVALID", "Недопустимый type: "+text,
                      offerPath(currentOfferId)+"/type"));
            }
          } else if ("commercial-type".equals(cur) && "offer".equals(parent)) {
            if (!COMMERCIAL_TYPE.contains(text)) {
              errors.add(err("COMMERCIAL_TYPE_INVALID", "Недопустимый commercial-type: "+text,
                      offerPath(currentOfferId)+"/commercial-type"));
            }
          } else if (("creation-date".equals(cur) || "last-update-date".equals(cur)) && "offer".equals(parent)) {
            if (!isValidIsoOffsetDateTime(text)) {
              errors.add(err("DATE_FORMAT", cur+" должен быть ISO-8601 с таймзоной",
                      offerPath(currentOfferId)+"/"+cur));
            }
          } else if ("url".equals(cur) && "offer".equals(parent)) {
            if (!isValidUrl(text)) {
              errors.add(err("URL_INVALID", "Некорректный url", offerPath(currentOfferId)+"/url"));
            }
          } else if ("currency".equals(cur) && "price".equals(parent)) {
            if (!text.matches("(?i)RUR|RUB|USD|EUR")) {
              warns.add(warn("CURRENCY_UNCOMMON", "Нестандартная валюта: "+text,
                      offerPath(currentOfferId)+"/price/currency"));
            }
          }

        } else if (e == XMLStreamConstants.END_ELEMENT) {
          String name = r.getLocalName();

          // Флаги наличия блоков — по закрытию нужных узлов
          if ("price".equals(name) && inOffer) hasPrice = true;
          if ("location".equals(name) && inOffer) hasLocation = true;

          if ("offer".equals(name)) {
            if (!hasType)     errors.add(err("TYPE_REQUIRED", "<type> обязателен", offerPath(currentOfferId)));
            if (!hasCategory) errors.add(err("CATEGORY_REQUIRED", "<category> обязателен", offerPath(currentOfferId)));
            if (!hasLocation) errors.add(err("LOCATION_REQUIRED", "<location> обязателен", offerPath(currentOfferId)));
            if (!hasPrice)    errors.add(err("PRICE_REQUIRED", "<price> обязателен", offerPath(currentOfferId)));
            inOffer = false;
            currentOfferId = null;
          }

          // снять текущий элемент со стека
          if (!path.isEmpty() && name.equals(path.peek())) {
            path.pop();
          } else {
            // на всякий случай, если попался «сломанный» XML
            while (!path.isEmpty() && !name.equals(path.peek())) path.pop();
            if (!path.isEmpty()) path.pop();
          }
        }
      }

    } catch (XMLStreamException ex) {
      errors.add(err("XML_PARSING", "Ошибка парсинга XML: " + ex.getMessage(), "/"));
    }

    return ValidationReport.builder()
            .valid(errors.isEmpty())
            .errors(errors)
            .warnings(warns)
            .build();
  }

  public PublishResponse publish(InputStream xml) throws IOException {
    // Читаем в память один раз, чтобы и проверить, и сохранить ровно этот же байтовый поток
    byte[] bytes = xml.readAllBytes();
    ValidationReport report = validate(new ByteArrayInputStream(bytes));

    if (!report.isValid()) {
      return PublishResponse.builder()
          .published(false)
          .report(report)
          .build();
    }

    String id = UUID.randomUUID().toString();
    Path dir = Paths.get(storageDir).toAbsolutePath().normalize();
    Files.createDirectories(dir);
    Path out = dir.resolve(id + ".xml");
    Files.write(out, bytes, StandardOpenOption.CREATE_NEW);

    return PublishResponse.builder()
        .published(true)
        .feedId(id)
        .url(baseUrl + "/" + id + ".xml")
        .report(report)
        .build();
  }

  // ==== helpers ====
  private static String getAttr(XMLStreamReader r, String name) {
    for (int i=0;i<r.getAttributeCount();i++) {
      if (name.equals(r.getAttributeLocalName(i))) return r.getAttributeValue(i);
    }
    return null;
  }
  private static ValidationReport.Issue err(String code, String msg, String path) {
    return ValidationReport.Issue.builder().code(code).message(msg).path(path).build();
  }
  private static ValidationReport.Issue warn(String code, String msg, String path) {
    return ValidationReport.Issue.builder().code(code).message(msg).path(path).build();
  }
  private static String offerPath(String id) {
    return id == null ? "/offer" : "/offer[@internal-id='"+id+"']";
  }
  private static boolean isValidIsoOffsetDateTime(String s) {
    try { java.time.OffsetDateTime.parse(s); return true; }
    catch (Exception e) { return false; }
  }
  private static boolean isValidUrl(String s) {
    try { new java.net.URL(s).toURI(); return true; }
    catch (Exception e) { return false; }
  }
}
