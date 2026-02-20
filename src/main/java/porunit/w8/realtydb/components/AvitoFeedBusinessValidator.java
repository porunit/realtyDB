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

            if (purpose == FeedPurpose.SALE) {
                if (l.getPrice() == null) {
                    errors.add(oid + ": нет Price для продажи");
                }
            } else {
                if (l.getMonthlyRent() == null) {
                    errors.add(oid + ": нет Price (monthlyRent) для аренды");
                }
            }

            String objectType = guessObjectType(l);
            if (isBlank(objectType)) {
                errors.add(oid + ": не удалось определить ObjectType");
            }

            String propertyRights = mapPropertyRights(l);
            if (isBlank(propertyRights)) {
                errors.add(oid + ": PropertyRights обязателен (Собственник/Посредник)");
            }

            if (requiresFloor(objectType) && l.getFloor() == null) {
                errors.add(oid + ": Floor обязателен для типа " + objectType);
            }

            if (requiresEntranceHard(objectType)) {
                if (mapEntrance(l) == null) {
                    errors.add(oid + ": Entrance обязателен для типа " + objectType);
                }
            }

            if (requiresLayout(objectType, purpose)) {
            }

            if (requiresDecoration(objectType)) {
                if (mapDecoration(l) == null) {
                    errors.add(oid + ": Decoration обязателен для типа " + objectType);
                }
            }

            if (isBlank(mapBuildingType(l))) {
                errors.add(oid + ": BuildingType обязателен");
            }

            if (requiresParkingType(objectType, purpose)) {
                if (mapParkingType(l) == null) {
                    errors.add(oid + ": ParkingType обязателен для типа " + objectType);
                }
            }

            if (purpose == FeedPurpose.SALE) {
                if (isBlank(mapTransactionType(l))) {
                    errors.add(oid + ": TransactionType обязателен для продажи");
                }
            } else {
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

    private String guessObjectType(Listing l) {
        var bt = l.getBuildingType() == null ? "" : l.getBuildingType().name().toLowerCase();
        if (bt.contains("office")) return "Офисное помещение";
        if (bt.contains("retail") || bt.contains("shop") || bt.contains("shopping")) {
            return "Торговое помещение";
        }
        return "Помещение свободного назначения";
    }

    private String mapPropertyRights(Listing l) {
        if (l.getOwnership() == null) return "Посредник";
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
        if (l.getParkingType() == null) return "Нет";
        return switch (l.getParkingType()) {
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
        if (l.getDealType() == null) return "Продажа";
        return switch (l.getDealType()) {
            case SALE -> "Продажа";
            case LEASE_ASSIGNMENT -> "Переуступка права аренды";
            default -> "Продажа";
        };
    }

}

