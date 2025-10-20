// porunit/w8/realtydb/data/PhotoMetaWithListingDto.java
package porunit.w8.realtydb.data;

import java.util.UUID;

public record PhotoMetaWithListingDto(
    UUID listingId, UUID id, String filename, String contentType, long sizeBytes, int position, boolean cover
) {
    public PhotoMetaDto toMeta() {
        return new PhotoMetaDto(id, filename, contentType, sizeBytes, position, cover);
    }
}
