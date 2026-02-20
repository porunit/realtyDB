package porunit.w8.realtydb.data.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ListingStatisticsDto(
        UUID listingId,
        Long viewsTotal,
        Long viewsSearch,
        Long viewsItem,
        ContactsDto contacts,
        List<ActivityEventDto> activityHistory,
        List<StatisticsByDateDto> dynamics
) {
    public ListingStatisticsDto {
        if (activityHistory == null) activityHistory = List.of();
        if (dynamics == null) dynamics = List.of();
    }
}
