package porunit.w8.realtydb.data.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityEventDto(
        OffsetDateTime at,
        String type,
        String description
) {}
