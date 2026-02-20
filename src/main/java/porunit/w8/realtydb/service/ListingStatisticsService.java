package porunit.w8.realtydb.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.data.statistics.ContactsDto;
import porunit.w8.realtydb.data.statistics.ListingStatisticsDto;
import porunit.w8.realtydb.client.ListingStatisticsClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListingStatisticsService {

    private final ListingService listingService;
    private final Optional<ListingStatisticsClient> statisticsClient;

    public ListingStatisticsService(ListingService listingService,
                                    Optional<ListingStatisticsClient> statisticsClient) {
        this.listingService = listingService;
        this.statisticsClient = statisticsClient;
    }

    @Transactional(readOnly = true)
    public ListingStatisticsDto getStatistics(UUID listingId) {
        listingService.findDtoById(listingId);

        ListingStatisticsDto remote = statisticsClient
                .map(client -> client.getStatistics(listingId))
                .orElse(null);
        if (remote != null) {
            return remote;
        }
        return stubStatistics(listingId);
    }

    private static ListingStatisticsDto stubStatistics(UUID listingId) {
        return new ListingStatisticsDto(
                listingId,
                0L,
                0L,
                0L,
                ContactsDto.empty(),
                List.of(),
                List.of()
        );
    }
}
