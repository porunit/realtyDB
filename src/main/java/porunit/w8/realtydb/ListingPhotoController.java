// porunit/w8/realtydb/ListingPhotoController.java
package porunit.w8.realtydb;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import porunit.w8.realtydb.data.ListingPhoto;
import porunit.w8.realtydb.data.PhotoMetaDto;
import porunit.w8.realtydb.service.ListingPhotoService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings/{listingId}/photos")
public class ListingPhotoController {

    private final ListingPhotoService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<PhotoMetaDto> upload(
            @PathVariable UUID listingId,
            @RequestPart("files") @NotEmpty List<MultipartFile> files
    ) throws IOException {
        // сохраняем как раньше, но отдаём метаданные «тонко»
        var saved = service.upload(listingId, files);
        // чтобы не тянуть entity -> JSON, просто перечитаем меты «тонко»
        return service.listMeta(listingId);
    }

    @GetMapping
    public List<PhotoMetaDto> list(@PathVariable UUID listingId) {
        return service.listMeta(listingId);
    }

    @GetMapping("/{photoId}/raw")
    public ResponseEntity<byte[]> downloadRaw(@PathVariable UUID listingId, @PathVariable UUID photoId) {
        ListingPhoto p = service.get(listingId, photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(p.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(p.getFilename() == null ? (photoId + ".bin") : p.getFilename())
                                .build().toString()
                )
                .body(p.getData());
    }

    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID listingId, @PathVariable UUID photoId) {
        service.delete(listingId, photoId);
    }

    @PutMapping("/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@PathVariable UUID listingId, @RequestBody List<UUID> orderedIds) {
        service.reorder(listingId, orderedIds);
    }

    @PutMapping("/{photoId}/cover")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCover(@PathVariable UUID listingId, @PathVariable UUID photoId) {
        service.setCover(listingId, photoId);
    }
}
