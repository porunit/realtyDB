package porunit.w8.realtydb.data.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactsDto(
        Long total,
        Long calls,
        Long messages,
        Long phoneRequests
) {
    public static ContactsDto empty() {
        return new ContactsDto(0L, 0L, 0L, 0L);
    }
}
