package porunit.w8.realtydb.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import porunit.w8.realtydb.data.statistics.ListingStatisticsDto;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "stats.service.enabled", havingValue = "true", matchIfMissing = true)
public class ListingStatisticsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String pathTemplate;

    public ListingStatisticsClient(
            @Qualifier("listingStatisticsRestTemplate") RestTemplate listingStatisticsRestTemplate,
            @Value("${stats.service.base-url:http://localhost:8082}") String baseUrl,
            @Value("${stats.service.path:/api/listings}") String path
    ) {
        this.restTemplate = listingStatisticsRestTemplate;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.pathTemplate = path.replaceAll("/$", "") + "/{listingId}/statistics";
    }

    public ListingStatisticsDto getStatistics(UUID listingId) {
        String url = baseUrl + pathTemplate.replace("{listingId}", listingId.toString());
        try {
            ResponseEntity<ListingStatisticsDto> response = restTemplate.getForEntity(url, ListingStatisticsDto.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ListingStatisticsDto body = response.getBody();
                if (body.listingId() == null) {
                    return new ListingStatisticsDto(
                            listingId,
                            body.viewsTotal(),
                            body.viewsSearch(),
                            body.viewsItem(),
                            body.contacts(),
                            body.activityHistory(),
                            body.dynamics()
                    );
                }
                return body;
            }
        } catch (RestClientException ignored) {
        }
        return null;
    }
}
