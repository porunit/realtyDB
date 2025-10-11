package porunit.w8.realtydb.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.data.Listing;
import porunit.w8.realtydb.repository.ListingRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository repository;

    public Listing create(Listing listing) {
        validateLeasedBlock(listing);
        return repository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Listing findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + id));
    }

    public Listing update(UUID id, Listing payload) {
        Listing existing = findById(id);
        // Прямое копирование полей (чтобы не усложнять мапперами)
        payload.setId(existing.getId());
        validateLeasedBlock(payload);
        return repository.save(payload);
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
            // если не сдано — очистим связанные поля
            l.setTenant(null);
            l.setMonthlyRent(null);
        }
    }
}
