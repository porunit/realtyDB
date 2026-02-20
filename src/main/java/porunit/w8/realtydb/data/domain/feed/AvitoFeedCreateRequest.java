package porunit.w8.realtydb.data.domain.feed;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AvitoFeedCreateRequest(
        @NotNull FeedPurpose purpose,
        @NotEmpty List<UUID> listingIds
) {}
