package porunit.w8.realtydb.data;

import java.util.UUID;

public record PhotoMetaDto(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        int position,
        boolean cover
) {}
