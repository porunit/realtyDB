package porunit.w8.realtydb.service;

import porunit.w8.realtydb.data.ListingPhoto;
import porunit.w8.realtydb.data.PhotoMetaDto;

public class PhotoMappers {
    public static PhotoMetaDto toMeta(ListingPhoto p) {
        return new PhotoMetaDto(
                p.getId(),
                p.getFilename(),
                p.getContentType(),
                p.getSizeBytes(),
                p.getPosition(),
                p.isCover()
        );
    }
}
