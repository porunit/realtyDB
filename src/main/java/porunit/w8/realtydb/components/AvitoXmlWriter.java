package porunit.w8.realtydb.components;

import org.springframework.stereotype.Component;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.ListingPhoto;
import porunit.w8.realtydb.data.domain.feed.FeedPurpose;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class AvitoXmlWriter {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public String generateXml(List<Listing> listings, FeedPurpose purpose) throws Exception {
        var sw = new StringWriter();
        var f = XMLOutputFactory.newFactory();
        var w = f.createXMLStreamWriter(sw);

        w.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        w.writeCharacters("\n");
        w.writeStartElement("Ads");
        w.writeAttribute("formatVersion", "3");
        w.writeAttribute("target", "Avito.ru");
        w.writeCharacters("\n");

        for (Listing l : listings) {
            writeAd(w, l, purpose);
        }

        w.writeEndElement(); // Ads
        w.writeCharacters("\n");
        w.writeEndDocument();
        w.flush();
        w.close();

        return sw.toString();
    }

    private void writeAd(XMLStreamWriter w, Listing l, FeedPurpose purpose) throws Exception {
        String objectType = mapObjectType(l);           // Вид объекта
        String operationType = mapOperationType(purpose); // "Продам"/"Сдам"
        String propertyRights = mapPropertyRights(l);   // "Собственник"/"Посредник"
        String entrance = mapEntrance(l);
        String decoration = mapDecoration(l);
        String buildingType = mapBuildingType(l);
        String parkingType = mapParkingType(l);
        String transactionType = mapTransactionType(l); // только для SALE
        String rentalType = mapRentalType(l);           // только для RENT

        w.writeStartElement("Ad");
        w.writeCharacters("\n");

        // Обязательные общие
        tag(w, "Id", l.getId().toString());
        writeDescription(w, l.getDescription());
        tag(w, "Address", l.getLocation());
        tag(w, "Category", "Коммерческая недвижимость");
        tag(w, "Price", purpose == FeedPurpose.SALE
                ? l.getPrice().toPlainString()
                : l.getMonthlyRent().toPlainString());
        tag(w, "OperationType", operationType); // "Продам"/"Сдам"
        tag(w, "ObjectType", objectType);       // "Офисное помещение", ...

        tag(w, "PropertyRights", propertyRights); // "Собственник"/"Посредник"

        // О помещении
        if (requiresEntranceHard(objectType)) {
            // обязателен
            tag(w, "Entrance", entrance);
        } else if (entrance != null) {
            // не обязателен, но можем вывести
            tag(w, "Entrance", entrance);
        }

        if (requiresFloor(objectType) && l.getFloor() != null) {
            tag(w, "Floor", l.getFloor().toString());
        }

        // Layout: только офис / (коворкинг в аренде)
        if (requiresLayout(objectType, purpose)) {
            writeLayout(w); // пока жёстко "Открытая"
        }

        // Square — обязателен всегда
        if (l.getArea() != null) {
            tag(w, "Square", l.getArea().toString());
        }

        // CeilingHeight — не всегда обязателен, но если есть — пишем
        if (l.getCeilingHeight() != null) {
            tag(w, "CeilingHeight", l.getCeilingHeight().toString());
        }

        // Decoration (обязательна для многих типов)
        if (requiresDecoration(objectType) && decoration != null) {
            tag(w, "Decoration", decoration);
        } else if (decoration != null) {
            tag(w, "Decoration", decoration);
        }

        // О здании
        tag(w, "BuildingType", buildingType);

        if (requiresParkingType(objectType, purpose) && parkingType != null) {
            tag(w, "ParkingType", parkingType);
        } else if (parkingType != null) {
            tag(w, "ParkingType", parkingType);
        }

        // Условия сделки
        if (purpose == FeedPurpose.SALE) {
            tag(w, "TransactionType", transactionType); // "Продажа"/"Переуступка права аренды"
            // Commission etc. можешь добавить позже (AgentSellCommissionSize)
        } else {
            tag(w, "RentalType", rentalType); // "Прямая"/"Субаренда"
            // AgentLeaseCommissionSize тоже можно добавить позже
        }

        // Фото
        writeImages(w, l);

        // Срок публикации (необязательные для Авито, но не вредят)
        tag(w, "DateBegin", nowIso());
        tag(w, "DateEnd", nowIsoPlusDays(30));

        w.writeEndElement(); // Ad
        w.writeCharacters("\n");
    }

    private void writeImages(XMLStreamWriter w, Listing l) throws Exception {
        var photos = l.getPhotos();
        if (photos == null || photos.isEmpty()) return;

        w.writeStartElement("Images");
        w.writeCharacters("\n");

        var sorted = photos.stream()
                .sorted(Comparator.comparingInt(p -> p.getPosition()))
                .toList();

        for (var p : sorted) {
            w.writeEmptyElement("Image");
            w.writeAttribute("url", buildPhotoUrl(l.getId(), p.getId()));
            w.writeCharacters("\n");
        }

        w.writeEndElement(); // Images
        w.writeCharacters("\n");
    }

    private void writeLayout(XMLStreamWriter w) throws Exception {
        // временно шьём всегда "Открытая"
        w.writeStartElement("Layout");
        w.writeCharacters("\n");
        w.writeStartElement("Option");
        w.writeCharacters("Открытая");
        w.writeEndElement();
        w.writeCharacters("\n");
        w.writeEndElement();
        w.writeCharacters("\n");
    }

    private void tag(XMLStreamWriter w, String tag, String text) throws Exception {
        if (text == null || text.isBlank()) return;
        w.writeStartElement(tag);
        w.writeCharacters(text);
        w.writeEndElement();
        w.writeCharacters("\n");
    }

    private String safeDescription(String desc) {
        // Авито просит CDATA для HTML, но plain текст без HTML допустим
        // мы пока отдаём как plain text
        return desc == null ? "" : desc;
    }

    private void writeDescription(XMLStreamWriter w, String desc) throws Exception {
        w.writeStartElement("Description");

        if (desc == null || desc.isBlank()) {
            // Пустое описание – формально Авито требует обязательное поле,
            // но если логика выше уже проверяет непустое – сюда обычно не попадём.
            w.writeCData("");
        } else {
            // ВАЖНО: пишем как есть, без XML-эскейпинга, внутри CDATA
            // Фронт должен следить, чтобы не было "]]>" внутри текста.
            w.writeCData(desc);
        }

        w.writeEndElement();
        w.writeCharacters("\n");
    }

    // ================= вспомогательные условия (должны совпадать с валидатором) ===============

    private boolean requiresFloor(String objectType) {
        return switch (objectType) {
            case "Офисное помещение",
                 "Помещение свободного назначения",
                 "Торговое помещение",
                 "Складское помещение",
                 "Производственное помещение",
                 "Помещение общественного питания",
                 "Гостиница",
                 "Автосервис",
                 "Коворкинг" -> true;
            default -> false;
        };
    }

    private boolean requiresEntranceHard(String objectType) {
        return switch (objectType) {
            case "Торговое помещение",
                 "Помещение свободного назначения",
                 "Помещение общественного питания" -> true;
            default -> false;
        };
    }

    private boolean requiresLayout(String objectType, FeedPurpose purpose) {
        if (objectType.equals("Офисное помещение")) return true;
        if (purpose == FeedPurpose.RENT && objectType.equals("Коворкинг")) return true;
        return false;
    }

    private boolean requiresDecoration(String objectType) {
        return switch (objectType) {
            case "Офисное помещение",
                 "Помещение свободного назначения",
                 "Торговое помещение",
                 "Помещение общественного питания",
                 "Гостиница",
                 "Здание" -> true;
            default -> false;
        };
    }

    private boolean requiresParkingType(String objectType, FeedPurpose purpose) {
        return switch (objectType) {
            case "Офисное помещение",
                 "Помещение свободного назначения",
                 "Торговое помещение",
                 "Помещение общественного питания",
                 "Гостиница",
                 "Здание",
                 "Коворкинг" -> true;
            default -> false;
        };
    }

    // ================= мапперы значений =================

    private String mapObjectType(Listing l) {
        var bt = l.getBuildingType() == null ? "" : l.getBuildingType().name().toLowerCase();
        if (bt.contains("office")) return "Офисное помещение";
        if (bt.contains("retail") || bt.contains("shopping") || bt.contains("shop")) {
            return "Торговое помещение";
        }
        // fallback
        return "Помещение свободного назначения";
    }

    private String mapOperationType(FeedPurpose p) {
        return p == FeedPurpose.SALE ? "Продам" : "Сдам";
    }

    private String mapPropertyRights(Listing l) {
        if (l.getOwnership() == null) return "Посредник";
        return switch (l.getOwnership()) {
            case OWNER -> "Собственник";
            default    -> "Посредник";
        };
    }

    private String mapEntrance(Listing l) {
        if (l.getEntrance() == null) return "С улицы";
        return switch (l.getEntrance()) {
            case STREET -> "С улицы";
            case COURTYARD -> "Со двора";
        };
    }


    private String mapDecoration(Listing l) {
        if (l.getFinishing() == null) return "Без отделки";
        return switch (l.getFinishing()) {
            case SHELL -> "Без отделки";
            case CLEAN -> "Чистовая";
            case OFFICE -> "Офисная";
        };
    }
    private String mapBuildingType(Listing l) {
        if (l.getBuildingType() == null) return "Другой";
        return switch (l.getBuildingType()) {
            case BUSINESS_CENTER -> "Бизнес-центр";
            case MALL -> "Торговый центр";
            case ADMIN_BUILDING -> "Административное здание";
            case RESIDENTIAL_COMPLEX -> "Жилой дом";
            default -> "Другой";
        };
    }

    private String mapParkingType(Listing l) {
        if (l.getParking() == null) return "Нет";
        return switch (l.getParking()) {
            case NONE     -> "Нет";
            case STREET   -> "На улице";
            case IN_BUILDING -> "В здании";
        };
    }

    // продажа
    private String mapTransactionType(Listing l) {
        if (l.getDealType() == null) return "Продажа"; // дефолт
        return switch (l.getDealType()) {
            case SALE -> "Продажа";
            case LEASE_ASSIGNMENT -> "Переуступка права аренды";
            default -> "Продажа";
        };
    }

    // аренда
    private String mapRentalType(Listing l) {
        if (l.getOwnership() == null) return "Прямая";
        return switch (l.getOwnership()) {
            case OWNER -> "Прямая";
            default    -> "Субаренда";
        };
    }

    private String buildPhotoUrl(UUID listingId, UUID photoId) {
        String base = System.getenv()
                .getOrDefault("PUBLIC_BASE_URL","https://plankton-app-equrn.ondigitalocean.app");
        return base + "/api/listings/" + listingId + "/photos/" + photoId + "/raw";
    }

    private String nowIso() {
        return OffsetDateTime.now().format(ISO_FORMATTER);
    }
    private String nowIsoPlusDays(int days) {
        return OffsetDateTime.now().plusDays(days).format(ISO_FORMATTER);
    }
}

