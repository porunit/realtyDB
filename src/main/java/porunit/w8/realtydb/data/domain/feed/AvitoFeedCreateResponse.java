package porunit.w8.realtydb.data.domain.feed;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AvitoFeedCreateResponse(
        UUID feedId,
        String xmlUrl,              // /api/feeds/avito/{feedId}.xml
        OffsetDateTime createdAt,
        String purpose,
        List<UUID> listings
) {}
