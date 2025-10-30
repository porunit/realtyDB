package porunit.w8.realtydb.components;

import org.springframework.stereotype.Component;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.feed.FeedPurpose;

import java.util.ArrayList;
import java.util.List;

@Component
public class AvitoFeedBusinessValidator {

    public record ValidationResult(boolean valid, List<String> errors) {}

    public ValidationResult validate(List<Listing> listings, FeedPurpose purpose) {
        List<String> errors = new ArrayList<>();

        if (listings.isEmpty()) {
            errors.add("Список объявлений пуст");
        }

        for (Listing l : listings) {
            if (l.getId() == null) {
                errors.add("Listing без id");
            }
            if (l.getTitle() == null || l.getTitle().isBlank()) {
                errors.add("Listing " + l.getId() + ": пустой title");
            }
            if (l.getLocation() == null || l.getLocation().isBlank()) {
                errors.add("Listing " + l.getId() + ": пустой address/location");
            }

            // Проверка цены
            if (purpose == FeedPurpose.SALE) {
                if (l.getPrice() == null) {
                    errors.add("Listing " + l.getId() + ": нет price для продажи");
                }
            } else {
                // RENT
                if (l.getMonthlyRent() == null) {
                    errors.add("Listing " + l.getId() + ": нет monthlyRent для аренды");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
