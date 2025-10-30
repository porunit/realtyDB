package porunit.w8.realtydb.components;

import org.springframework.stereotype.Component;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.feed.FeedPurpose;

import java.util.ArrayList;
import java.util.List;

@Component
public class AvitoFeedBusinessValidator {

    public record ValidationResult(boolean valid, List<String> errors) {
    }

    public ValidationResult validate(List<Listing> listings, FeedPurpose purpose) {
        List<String> errors = new ArrayList<>();

        if (listings.isEmpty()) {
            errors.add("Список объявлений пуст");
        }

        for (Listing l : listings) {
            String oid = l.getId() == null ? "<no-id>" : l.getId().toString();

            // базовые обязательные
            if (isBlank(l.getTitle())) {
                errors.add(oid + ": пустой Title");
            }
            if (isBlank(l.getDescription())) {
                errors.add(oid + ": пустой Description");
            }
            if (isBlank(l.getLocation())) {
                errors.add(oid + ": пустой Address");
            }
            if (l.getArea() == null) {
                errors.add(oid + ": нет Square (area)");
            }

            // цена
            if (purpose == FeedPurpose.SALE) {
                if (l.getPrice() == null) {
                    errors.add(oid + ": нет Price для продажи");
                }
            } else { // RENT
                if (l.getMonthlyRent() == null) {
                    errors.add(oid + ": нет Price (monthlyRent) для аренды");
                }
            }

            // OperationType — у нас всегда "Продам" или "Сдам" => ок

            // ObjectType
            String objectType = guessObjectType(l);
            if (isBlank(objectType)) {
                errors.add(oid + ": не удалось определить ObjectType");
            }

            // PropertyRights
            String propertyRights = mapPropertyRights(l);
            if (isBlank(propertyRights)) {
                errors.add(oid + ": PropertyRights обязателен (Собственник/Посредник)");
            }

            // Floor (если применимо)
            if (requiresFloor(objectType) && l.getFloor() == null) {
                errors.add(oid + ": Floor обязателен для типа " + objectType);
            }

            // Entrance (если обязателен)
            if (requiresEntranceHard(objectType)) {
                if (mapEntrance(l) == null) {
                    errors.add(oid + ": Entrance обязателен для типа " + objectType);
                }
            }

            // Layout (обязательно для офисного помещения (и коворкинга в аренде, но пока коворкинг не мапим))
            if (requiresLayout(objectType, purpose)) {
                // у нас сейчас Layout постоянный "Открытая"
                // если захотим сделать поле – будем проверять по нему.
                // Пока мы всегда будем писать Layout для офисов, без ошибок.
            }

            // Decoration (обязателен для ряда типов)
            if (requiresDecoration(objectType)) {
                if (mapDecoration(l) == null) {
                    errors.add(oid + ": Decoration обязателен для типа " + objectType);
                }
            }

            // BuildingType (обязателен всегда)
            if (isBlank(mapBuildingType(l))) {
                errors.add(oid + ": BuildingType обязателен");
            }

            // ParkingType (обязателен для ряда типов)
            if (requiresParkingType(objectType, purpose)) {
                if (mapParkingType(l) == null) {
                    errors.add(oid + ": ParkingType обязателен для типа " + objectType);
                }
            }

            if (purpose == FeedPurpose.SALE) {
                // TransactionType обязателен
                if (isBlank(mapTransactionType(l))) {
                    errors.add(oid + ": TransactionType обязателен для продажи");
                }
            } else {
                // RENT → RentalType обязателен
                if (isBlank(mapRentalType(l))) {
                    errors.add(oid + ": RentalType обязателен для аренды");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
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

    // Entrance обязателен именно для (строго):
    // "Торговое помещение", "Помещение свободного назначения", "Помещение общественного питания"
    private boolean requiresEntranceHard(String objectType) {
        return switch (objectType) {
            case "Торговое помещение",
                 "Помещение свободного назначения",
                 "Помещение общественного питания" -> true;
            default -> false;
        };
    }

    private String mapEntrance(Listing l) {
        if (l.getEntrance() == null) return "С улицы";
        return switch (l.getEntrance()) {
            case STREET -> "С улицы";
            case COURTYARD -> "Со двора";
        };
    }


    // Layout обязателен для "Офисное помещение", и (в аренде) "Коворкинг"
    private boolean requiresLayout(String objectType, FeedPurpose purpose) {
        if (objectType.equals("Офисное помещение")) return true;
        if (purpose == FeedPurpose.RENT && objectType.equals("Коворкинг")) return true;
        return false;
    }

    // Decoration обязателен для:
    // "Офисное помещение", "Помещение свободного назначения", "Торговое помещение",
    // "Помещение общественного питания", "Гостиница", "Здание"
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

    // ParkingType обязателен для:
    // продажа: офис, псн, торговое, общепит, гостиница, здание
    // аренда: те же + коворкинг
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

    // --- вспомогательные мапперы (эти должны повторять логику AvitoXmlWriter):

    private String guessObjectType(Listing l) {
        // синхронно mapObjectType из writer
        var bt = l.getBuildingType() == null ? "" : l.getBuildingType().name().toLowerCase();
        if (bt.contains("office")) return "Офисное помещение";
        if (bt.contains("retail") || bt.contains("shop") || bt.contains("shopping")) {
            return "Торговое помещение";
        }
        return "Помещение свободного назначения";
    }

    private String mapPropertyRights(Listing l) {
        // см. маппер выше в тексте, тут дублируем либо вынеси в утилиту
        if (l.getOwnership() == null) return "Посредник";
        // пример:
        return switch (l.getOwnership()) {
            case OWNER -> "Собственник";
            default -> "Посредник";
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

    private String mapRentalType(Listing l) {
        if (l.getOwnership() == null) return "Прямая";
        return switch (l.getOwnership()) {
            case OWNER -> "Прямая";
            default -> "Субаренда";
        };
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String mapTransactionType(Listing l) {
        if (l.getDealType() == null) return "Продажа"; // дефолт
        return switch (l.getDealType()) {
            case SALE -> "Продажа";
            case LEASE_ASSIGNMENT -> "Переуступка права аренды";
            default -> "Продажа";
        };
    }

}

