package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.ListingPhoto;
import porunit.w8.realtydb.data.PhotoMetaDto;
import porunit.w8.realtydb.repository.ListingPhotoRepository;
import porunit.w8.realtydb.repository.ListingRepository;


import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingPhotoService {

    private static final int MAX_PHOTOS = 9;

    private final ListingRepository listingRepository;
    private final ListingPhotoRepository photoRepository;

    @Transactional(readOnly = true)
    public List<ListingPhoto> list(UUID listingId) {
        Listing listing = findListing(listingId);
        return photoRepository.findByListingOrderByPositionAscCreatedAtAsc(listing);
    }
    // ... остальной код без изменений ...
    @Transactional(readOnly = true)
    public List<PhotoMetaDto> listMeta(UUID listingId) {
        return photoRepository.findMetaByListingId(listingId);
    }

    public List<ListingPhoto> upload(UUID listingId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return List.of();

        Listing listing = findListing(listingId);
        long current = photoRepository.countByListing(listing);
        if (current + files.size() > MAX_PHOTOS) {
            throw new IllegalArgumentException("Too many photos: max " + MAX_PHOTOS);
        }

        // найти следующее доступное position (0..8)
        List<ListingPhoto> existing = photoRepository.findByListingOrderByPositionAscCreatedAtAsc(listing);
        Set<Integer> used = new HashSet<>();
        existing.forEach(p -> used.add(p.getPosition()));

        List<ListingPhoto> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;

            int pos = nextFreePosition(used);
            used.add(pos);

            ListingPhoto p = ListingPhoto.builder()
                    .listing(listing)
                    .data(f.getBytes())
                    .contentType(Optional.ofNullable(f.getContentType()).orElse("application/octet-stream"))
                    .filename(f.getOriginalFilename())
                    .sizeBytes(f.getSize())
                    .position(pos)
                    .cover(existing.isEmpty() && saved.isEmpty() && pos == 0) // если первая — делаем обложкой
                    .build();

            saved.add(photoRepository.save(p));
        }
        return saved;
    }

    public void delete(UUID listingId, UUID photoId) {
        Listing listing = findListing(listingId);
        ListingPhoto p = photoRepository.findByIdAndListing(photoId, listing)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));
        photoRepository.delete(p);
        compactPositions(listing); // сжать позиции 0..N-1
    }

    public void setCover(UUID listingId, UUID photoId) {
        Listing listing = findListing(listingId);
        List<ListingPhoto> photos = photoRepository.findByListingOrderByPositionAscCreatedAtAsc(listing);
        boolean found = false;
        for (ListingPhoto p : photos) {
            boolean isCover = p.getId().equals(photoId);
            p.setCover(isCover);
            if (isCover) found = true;
        }
        if (!found) throw new EntityNotFoundException("Photo not found");
        // опционально: ставим обложку на позицию 0
        photos.stream().filter(p -> p.getId().equals(photoId)).findFirst().ifPresent(p -> p.setPosition(0));
        // остальные — сдвинуть после нулевой (с сохранением относительного порядка)
        int idx = 1;
        for (ListingPhoto p : photos) {
            if (!p.getId().equals(photoId)) {
                p.setPosition(idx++);
            }
        }
        photoRepository.saveAll(photos);
    }

    public void reorder(UUID listingId, List<UUID> orderedIds) {
        Listing listing = findListing(listingId);
        List<ListingPhoto> photos = photoRepository.findByListingOrderByPositionAscCreatedAtAsc(listing);
        if (orderedIds.size() != photos.size()) {
            throw new IllegalArgumentException("orderedIds size must equal photos count");
        }
        Map<UUID, ListingPhoto> map = new HashMap<>();
        photos.forEach(p -> map.put(p.getId(), p));

        for (int i = 0; i < orderedIds.size(); i++) {
            UUID id = orderedIds.get(i);
            ListingPhoto p = map.get(id);
            if (p == null) throw new EntityNotFoundException("Photo not found: " + id);
            p.setPosition(i);
            p.setCover(i == 0); // позиция 0 — обложка
        }
        photoRepository.saveAll(photos);
    }

    @Transactional(readOnly = true)
    public ListingPhoto get(UUID listingId, UUID photoId) {
        Listing listing = findListing(listingId);
        return photoRepository.findByIdAndListing(photoId, listing)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));
    }

    // helpers

    private Listing findListing(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + id));
    }

    private int nextFreePosition(Set<Integer> used) {
        for (int i = 0; i < MAX_PHOTOS; i++) {
            if (!used.contains(i)) return i;
        }
        // не должно случаться — контролируем сверху
        return MAX_PHOTOS - 1;
    }

    private void compactPositions(Listing listing) {
        List<ListingPhoto> photos = photoRepository.findByListingOrderByPositionAscCreatedAtAsc(listing);
        int i = 0;
        for (ListingPhoto p : photos) {
            p.setPosition(i++);
        }
        if (!photos.isEmpty()) {
            // первая — обложка
            photos.get(0).setCover(true);
            for (int j = 1; j < photos.size(); j++) photos.get(j).setCover(false);
        }
        photoRepository.saveAll(photos);
    }
}
