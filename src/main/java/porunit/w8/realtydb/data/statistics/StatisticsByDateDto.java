package porunit.w8.realtydb.data.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatisticsByDateDto(
        LocalDate date,
        Long views,
        Long viewsSearch,
        Long viewsItem,
        Long contacts
) {}
