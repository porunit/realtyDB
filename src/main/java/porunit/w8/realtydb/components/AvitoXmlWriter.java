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

@Component
public class AvitoXmlWriter {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public String generateXml(List<Listing> listings, FeedPurpose purpose) throws Exception {
        var out = new StringWriter();
        XMLOutputFactory f = XMLOutputFactory.newFactory();
        XMLStreamWriter w = f.createXMLStreamWriter(out);

        w.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        w.writeCharacters("\n");
        w.writeStartElement("Ads");
        w.writeAttribute("formatVersion", "3");
        w.writeAttribute("target", "Avito.ru");
        w.writeCharacters("\n");

        for (Listing l : listings) {
            writeAd(w, l, purpose);
        }

        w.writeEndElement(); // </Ads>
        w.writeCharacters("\n");
        w.writeEndDocument();
        w.flush();
        w.close();

        return out.toString();
    }

    private void writeAd(XMLStreamWriter w, Listing l, FeedPurpose purpose) throws Exception {
        w.writeStartElement("Ad");
        w.writeCharacters("\n");

        tag(w, "Id", l.getId().toString());
        tag(w, "Category", "Коммерческая недвижимость");
        tag(w, "OperationType", purpose == FeedPurpose.SALE ? "Продажа" : "Аренда");
        tag(w, "CommercialType", mapCommercialType(l));

        tagIfNotBlank(w, "Title", l.getTitle());
        tagIfNotBlank(w, "Description", l.getDescription());
        tagIfNotBlank(w, "Address", l.getLocation());

        if (l.getArea() != null) {
            tag(w, "Square", l.getArea().toString());
        }

        // Цена
        if (purpose == FeedPurpose.SALE && l.getPrice() != null) {
            tag(w, "Price", l.getPrice().toPlainString());
            tag(w, "PriceType", "FIXED");
        } else if (purpose == FeedPurpose.RENT && l.getMonthlyRent() != null) {
            tag(w, "Price", l.getMonthlyRent().toPlainString());
            tag(w, "RentPeriod", "месяц");
        }

        if (l.getFloor() != null)
            tag(w, "Floor", l.getFloor().toString());
        if (l.getCeilingHeight() != null)
            tag(w, "CeilingHeight", l.getCeilingHeight().toString());

        writeImages(w, l.getId().toString(), l.getPhotos());

        String now = nowIso();
        String plus30 = nowIsoPlusDays(30);
        tag(w, "DateBegin", now);
        tag(w, "DateEnd", plus30);

        w.writeEndElement(); // </Ad>
        w.writeCharacters("\n");
    }

    private void writeImages(XMLStreamWriter w, String listingId, List<ListingPhoto> photos) throws Exception {
        if (photos == null || photos.isEmpty()) return;

        w.writeStartElement("Images");
        w.writeCharacters("\n");

        var sorted = photos.stream()
                .sorted(Comparator.comparingInt(p -> p.getPosition()))
                .toList();

        for (ListingPhoto p : sorted) {
            w.writeEmptyElement("Image");
            w.writeAttribute("url", buildPhotoUrl(listingId, p.getId().toString()));
            w.writeCharacters("\n");
        }

        w.writeEndElement(); // </Images>
        w.writeCharacters("\n");
    }

    private void tag(XMLStreamWriter w, String tag, String text) throws Exception {
        w.writeStartElement(tag);
        w.writeCharacters(text);
        w.writeEndElement();
        w.writeCharacters("\n");
    }

    private void tagIfNotBlank(XMLStreamWriter w, String tag, String text) throws Exception {
        if (text == null || text.isBlank()) return;
        tag(w, tag, text);
    }

    private String mapCommercialType(Listing l) {
        // Упростим приматч:
        // Офисы → "Офисное помещение"
        // Ритейл/торговля/стрит-ритейл → "Торговое помещение"
        // всё остальное → "Помещение свободного назначения"
        var bt = l.getBuildingType() == null ? "" : l.getBuildingType().name().toLowerCase();
        if (bt.contains("office")) return "Офисное помещение";
        if (bt.contains("retail") || bt.contains("shop") || bt.contains("shopping")) {
            return "Торговое помещение";
        }
        return "Помещение свободного назначения";
    }

    private String buildPhotoUrl(String listingId, String photoId) {
        // PUBLIC_BASE_URL мы можем прокинуть через env
        String base = System.getenv().getOrDefault(
                "PUBLIC_BASE_URL",
                "https://plankton-app-equrn.ondigitalocean.app"
        );
        return base + "/api/listings/" + listingId + "/photos/" + photoId + "/raw";
    }

    private String nowIso() {
        return OffsetDateTime.now().format(ISO_FORMATTER);
    }

    private String nowIsoPlusDays(int days) {
        return OffsetDateTime.now().plusDays(days).format(ISO_FORMATTER);
    }
}
