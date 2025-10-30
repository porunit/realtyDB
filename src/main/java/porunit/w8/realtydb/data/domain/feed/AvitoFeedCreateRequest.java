package porunit.w8.realtydb.data.domain.feed;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AvitoFeedCreateRequest(
        @NotNull FeedPurpose purpose,        // SALE или RENT
        @NotEmpty List<UUID> listingIds      // какие объявления включить в фид
) {}
