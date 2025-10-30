package porunit.w8.realtydb.data.domain.feed;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AvitoFeedListItemDto(
        UUID feedId,
        OffsetDateTime createdAt,
        String purpose,
        int listingCount,
        String xmlUrl
) {}
