package porunit.w8.realtydb.components;

import org.springframework.stereotype.Component;
import porunit.w8.realtydb.data.domain.Listing;
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

        w.writeEndElement();
        w.writeCharacters("\n");
        w.writeEndDocument();
        w.flush();
        w.close();

        return sw.toString();
    }

    private void writeAd(XMLStreamWriter w, Listing l, FeedPurpose purpose) throws Exception {
        String objectType = mapObjectType(l);
        String operationType = mapOperationType(purpose);
        String propertyRights = mapPropertyRights(l);
        String entrance = mapEntrance(l);
        String decoration = mapDecoration(l);
        String buildingType = mapBuildingType(l);
        String parkingType = mapParkingType(l);
        String transactionType = mapTransactionType(l);
        String rentalType = mapRentalType(l);

        w.writeStartElement("Ad");
        w.writeCharacters("\n");

        tag(w, "Id", l.getId().toString());
        writeDescription(w, l.getDescription());
        tag(w, "Title", l.getTitle());
        tag(w, "Address", l.getLocation());
        tag(w, "Category", "Коммерческая недвижимость");
        tag(w, "Price", purpose == FeedPurpose.SALE
                ? l.getPrice().toPlainString()
                : l.getMonthlyRent().toPlainString());
        tag(w, "OperationType", operationType);
        tag(w, "ObjectType", objectType);

        tag(w, "PropertyRights", propertyRights);

        if (requiresEntranceHard(objectType)) {
            tag(w, "Entrance", entrance);
        } else if (entrance != null) {
            tag(w, "Entrance", entrance);
        }

        if (requiresFloor(objectType) && l.getFloor() != null) {
            tag(w, "Floor", l.getFloor().toString());
        }

        if (requiresLayout(objectType, purpose)) {
            writeLayout(w);
        }

        if (l.getArea() != null) {
            tag(w, "Square", l.getArea().toString());
        }

        if (l.getCeilingHeight() != null) {
            tag(w, "CeilingHeight", l.getCeilingHeight().toString());
        }

        if (requiresDecoration(objectType) && decoration != null) {
            tag(w, "Decoration", decoration);
        } else if (decoration != null) {
            tag(w, "Decoration", decoration);
        }

        tag(w, "BuildingType", buildingType);

        if (requiresParkingType(objectType, purpose) && parkingType != null) {
            tag(w, "ParkingType", parkingType);
        } else if (parkingType != null) {
            tag(w, "ParkingType", parkingType);
        }

        if (purpose == FeedPurpose.SALE) {
            tag(w, "TransactionType", transactionType);
        } else {
            tag(w, "RentalType", rentalType);
        }

        writeImages(w, l);

        tag(w, "DateBegin", nowIso());
        tag(w, "DateEnd", nowIsoPlusDays(30));

        w.writeEndElement();
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

        w.writeEndElement();
        w.writeCharacters("\n");
    }

    private void writeLayout(XMLStreamWriter w) throws Exception {
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
        return desc == null ? "" : desc;
    }

    private void writeDescription(XMLStreamWriter w, String desc) throws Exception {
        w.writeStartElement("Description");

        if (desc == null || desc.isBlank()) {
            w.writeCData("");
        } else {
            w.writeCData(desc);
        }

        w.writeEndElement();
        w.writeCharacters("\n");
    }

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

    private String mapObjectType(Listing l) {
        var bt = l.getBuildingType() == null ? "" : l.getBuildingType().name().toLowerCase();
        if (bt.contains("office")) return "Офисное помещение";
        if (bt.contains("retail") || bt.contains("shopping") || bt.contains("shop")) {
            return "Торговое помещение";
        }
        return "Помещение свободного назначения";
    }

    private String mapOperationType(FeedPurpose p) {
        return p == FeedPurpose.SALE ? "Продам" : "Сдам";
    }

    private String mapPropertyRights(Listing l) {
        if (l.getOwnership() == null) return "Посредник";
        return switch (l.getOwnership()) {
            case OWNER -> "Собственник";
            default -> "Посредник";
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
        if (l.getParkingType() == null) return "Нет";
        return switch (l.getParkingType()) {
            case NONE -> "Нет";
            case STREET -> "На улице";
            case IN_BUILDING -> "В здании";
        };
    }

    private String mapTransactionType(Listing l) {
        if (l.getDealType() == null) return "Продажа";
        return switch (l.getDealType()) {
            case SALE -> "Продажа";
            case LEASE_ASSIGNMENT -> "Переуступка права аренды";
            default -> "Продажа";
        };
    }

    private String mapRentalType(Listing l) {
        if (l.getOwnership() == null) return "Прямая";
        return switch (l.getOwnership()) {
            case OWNER -> "Прямая";
            default -> "Субаренда";
        };
    }

    private String buildPhotoUrl(UUID listingId, UUID photoId) {
        String base = System.getenv()
                .getOrDefault("PUBLIC_BASE_URL", "https://plankton-app-equrn.ondigitalocean.app");
        return base + "/api/listings/" + listingId + "/photos/" + photoId + "/raw";
    }

    private String nowIso() {
        return OffsetDateTime.now().format(ISO_FORMATTER);
    }

    private String nowIsoPlusDays(int days) {
        return OffsetDateTime.now().plusDays(days).format(ISO_FORMATTER);
    }
}

