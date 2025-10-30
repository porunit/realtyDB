// porunit/w8/realtydb/service/ListingService.java
package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.data.*;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.repository.ListingPhotoRepository;
import porunit.w8.realtydb.repository.ListingRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository repository;
    private final ListingPhotoRepository photoRepository;

    public Listing create(Listing listing) {
        validateLeasedBlock(listing);
        return repository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> findAll() {
        return repository.findAll();
    }

    // Новый метод: «тонкая» выдача всех объявлений как DTO + метаданные фото
    @Transactional(readOnly = true)
    public List<ListingDto> findAllDto() {
        List<Listing> all = repository.findAll();
        if (all.isEmpty()) return List.of();

        List<UUID> ids = all.stream().map(Listing::getId).toList();
        List<PhotoMetaWithListingDto> metas = photoRepository.findMetaByListingIdIn(ids);

        Map<UUID, List<PhotoMetaDto>> grouped = metas.stream()
                .collect(Collectors.groupingBy(
                        PhotoMetaWithListingDto::listingId,
                        Collectors.mapping(PhotoMetaWithListingDto::toMeta, Collectors.toList())
                ));

        List<ListingDto> out = new ArrayList<>(all.size());
        for (Listing l : all) {
            List<PhotoMetaDto> photos = grouped.getOrDefault(l.getId(), List.of());
            out.add(ListingMappers.toDto(l, photos));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Listing findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + id));
    }

    @Transactional(readOnly = true)
    public ListingDto findDtoById(UUID id) {
        Listing l = findById(id);
        List<PhotoMetaDto> photos = photoRepository.findMetaByListingId(id);
        return ListingMappers.toDto(l, photos);
    }

    public Listing update(UUID id, Listing payload) {
        Listing existing = findById(id);

        // Копируем ТОЛЬКО скаляры (без id, photos, createdAt/updatedAt и т.п.)
        existing.setTitle(payload.getTitle());
        existing.setDescription(payload.getDescription());
        existing.setPrice(payload.getPrice());
        existing.setLocation(payload.getLocation());
        existing.setOwnership(payload.getOwnership());
        existing.setEmail(payload.getEmail());
        existing.setCompanyName(payload.getCompanyName());
        existing.setPhone(payload.getPhone());
        existing.setEntrance(payload.getEntrance());
        existing.setFloor(payload.getFloor());
        existing.setArea(payload.getArea());
        existing.setCeilingHeight(payload.getCeilingHeight());
        existing.setFinishing(payload.getFinishing());
        existing.setPowerKw(payload.getPowerKw());
        existing.setHeating(payload.getHeating());
        existing.setReadiness(payload.getReadiness());
        existing.setBuildingType(payload.getBuildingType());
        existing.setRoadDistance(payload.getRoadDistance());
        existing.setParking(payload.getParking());
        existing.setDealType(payload.getDealType());
        existing.setLeased(payload.getLeased());
        existing.setTenant(payload.getTenant());
        existing.setMonthlyRent(payload.getMonthlyRent());
        existing.setAgentCommission(payload.getAgentCommission());
        existing.setVatIncluded(payload.getVatIncluded());

        // Валидируем уже "собранное" состояние
        validateLeasedBlock(existing);

        // ✅ сохраняем ИМЕННО existing — коллекция photos не меняется
        return repository.save(existing);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Listing not found: " + id);
        }
        repository.deleteById(id);
    }

    private void validateLeasedBlock(Listing l) {
        if (Boolean.TRUE.equals(l.getLeased())) {
            if (l.getTenant() == null || l.getTenant().isBlank()) {
                throw new IllegalArgumentException("tenant is required when leased = true");
            }
            if (l.getMonthlyRent() == null) {
                throw new IllegalArgumentException("monthlyRent is required when leased = true");
            }
        } else {
            l.setTenant(null);
            l.setMonthlyRent(null);
        }
    }
}
