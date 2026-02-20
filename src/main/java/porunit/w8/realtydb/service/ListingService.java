package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.data.*;
import porunit.w8.realtydb.data.domain.*;
import porunit.w8.realtydb.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingService {

    private final ListingRepository repository;
    private final ListingPhotoRepository photoRepository;
    private final OwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final DealRepository dealRepository;
    private final ParkingRepository parkingRepository;
    private final ReadinessRepository readinessRepository;
    private final UserService userService;

    public Listing create(ListingRequest req) {
        User current = userService.requireCurrentUser();
        if (current.getId() == null) {
            throw new IllegalStateException("Config admin cannot create listings. Register an admin user and use it to create listings.");
        }
        Owner owner = ownerRepository.save(Owner.builder()
                .name(req.ownerName())
                .ownershipType(req.ownershipType() != null ? req.ownershipType() : OwnershipType.OWNER)
                .build());
        Building building = buildBuilding(req);
        if (building != null) building = buildingRepository.save(building);
        Deal deal = buildDeal(req);
        if (deal != null) deal = dealRepository.save(deal);
        ParkingEntity parking = buildParking(req);
        if (parking != null) parking = parkingRepository.save(parking);
        ReadinessEntity readiness = buildReadiness(req);
        if (readiness != null) readiness = readinessRepository.save(readiness);

        Listing listing = Listing.builder()
                .title(req.title())
                .description(req.description())
                .price(req.price())
                .location(req.location())
                .owner(owner)
                .email(req.email())
                .companyName(req.companyName())
                .phone(req.phone())
                .entranceType(req.entranceType())
                .floor(req.floor())
                .area(req.area())
                .ceilingHeight(req.ceilingHeight())
                .finishing(req.finishing())
                .powerKw(req.powerKw())
                .heating(req.heating())
                .readiness(readiness)
                .building(building)
                .roadDistance(req.roadDistance())
                .parking(parking)
                .deal(deal)
                .leased(req.leased())
                .tenant(req.tenant())
                .monthlyRent(req.monthlyRent())
                .agentCommission(req.agentCommission())
                .vatIncluded(req.vatIncluded())
                .createdBy(current)
                .build();
        validateLeasedBlock(listing);
        return repository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ListingDto> findAllDto() {
        User current = userService.requireCurrentUser();
        List<Listing> all = current.getRole() == Role.ADMIN
                ? repository.findAll()
                : repository.findByCreatedByOrderByCreatedAtDesc(current);
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
            out.add(ListingMappers.toDto(l, grouped.getOrDefault(l.getId(), List.of())));
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
        User current = userService.requireCurrentUser();
        if (current.getRole() != Role.ADMIN && (l.getCreatedBy() == null || !l.getCreatedBy().getId().equals(current.getId()))) {
            throw new EntityNotFoundException("Listing not found: " + id);
        }
        List<PhotoMetaDto> photos = photoRepository.findMetaByListingId(id);
        return ListingMappers.toDto(l, photos);
    }

    public Listing update(UUID id, ListingRequest req) {
        Listing existing = findById(id);
        User current = userService.requireCurrentUser();
        if (current.getRole() != Role.ADMIN && (existing.getCreatedBy() == null || !existing.getCreatedBy().getId().equals(current.getId()))) {
            throw new EntityNotFoundException("Listing not found: " + id);
        }

        existing.getOwner().setName(req.ownerName());
        existing.getOwner().setOwnershipType(req.ownershipType() != null ? req.ownershipType() : OwnershipType.OWNER);
        ownerRepository.save(existing.getOwner());

        if (req.buildingType() != null || req.developerName() != null || req.floorsCount() != null) {
            Building b = existing.getBuilding();
            if (b == null) {
                b = buildBuilding(req);
                if (b != null) existing.setBuilding(buildingRepository.save(b));
            } else {
                if (req.buildingType() != null) b.setBuildingType(req.buildingType());
                b.setDeveloperName(req.developerName());
                b.setFloorsCount(req.floorsCount());
                buildingRepository.save(b);
            }
        }
        if (req.dealType() != null || req.contractNumber() != null || req.contractDate() != null || req.commissionPercent() != null) {
            Deal d = existing.getDeal();
            if (d == null) {
                d = buildDeal(req);
                if (d != null) existing.setDeal(dealRepository.save(d));
            } else {
                if (req.dealType() != null) d.setDealType(req.dealType());
                d.setContractNumber(req.contractNumber());
                d.setContractDate(req.contractDate());
                d.setCommissionPercent(req.commissionPercent());
                dealRepository.save(d);
            }
        }
        if (req.parkingType() != null || req.parkingTotalSpaces() != null || req.parkingAvailableSpaces() != null
                || req.parkingHourlyRate() != null || req.parkingMonthlyRate() != null) {
            ParkingEntity p = existing.getParking();
            if (p == null) {
                p = buildParking(req);
                if (p != null) existing.setParking(parkingRepository.save(p));
            } else {
                if (req.parkingType() != null) p.setParkingType(req.parkingType());
                p.setTotalSpaces(req.parkingTotalSpaces());
                p.setAvailableSpaces(req.parkingAvailableSpaces());
                p.setHourlyRate(req.parkingHourlyRate());
                p.setMonthlyRate(req.parkingMonthlyRate());
                parkingRepository.save(p);
            }
        }
        if (req.readinessType() != null || req.expectedCompletion() != null || req.actualCompletion() != null
                || req.currentStage() != null || req.completionPercent() != null) {
            ReadinessEntity r = existing.getReadiness();
            if (r == null) {
                r = buildReadiness(req);
                if (r != null) existing.setReadiness(readinessRepository.save(r));
            } else {
                if (req.readinessType() != null) r.setReadinessType(req.readinessType());
                r.setExpectedCompletion(req.expectedCompletion());
                r.setActualCompletion(req.actualCompletion());
                r.setCurrentStage(req.currentStage());
                r.setCompletionPercent(req.completionPercent());
                readinessRepository.save(r);
            }
        }

        existing.setTitle(req.title());
        existing.setDescription(req.description());
        existing.setPrice(req.price());
        existing.setLocation(req.location());
        existing.setEmail(req.email());
        existing.setCompanyName(req.companyName());
        existing.setPhone(req.phone());
        existing.setEntranceType(req.entranceType());
        existing.setFloor(req.floor());
        existing.setArea(req.area());
        existing.setCeilingHeight(req.ceilingHeight());
        existing.setFinishing(req.finishing());
        existing.setPowerKw(req.powerKw());
        existing.setHeating(req.heating());
        existing.setRoadDistance(req.roadDistance());
        existing.setLeased(req.leased());
        existing.setTenant(req.tenant());
        existing.setMonthlyRent(req.monthlyRent());
        existing.setAgentCommission(req.agentCommission());
        existing.setVatIncluded(req.vatIncluded());
        validateLeasedBlock(existing);
        return repository.save(existing);
    }

    public void delete(UUID id) {
        Listing existing = findById(id);
        User current = userService.requireCurrentUser();
        if (current.getRole() != Role.ADMIN && (existing.getCreatedBy() == null || !existing.getCreatedBy().getId().equals(current.getId()))) {
            throw new EntityNotFoundException("Listing not found: " + id);
        }
        repository.deleteById(id);
    }

    private Building buildBuilding(ListingRequest req) {
        if (req.buildingType() == null && req.developerName() == null && req.floorsCount() == null) return null;
        return Building.builder()
                .buildingType(req.buildingType() != null ? req.buildingType() : BuildingType.OTHER)
                .developerName(req.developerName())
                .floorsCount(req.floorsCount())
                .build();
    }

    private Deal buildDeal(ListingRequest req) {
        if (req.dealType() == null && req.contractNumber() == null && req.contractDate() == null && req.commissionPercent() == null) return null;
        return Deal.builder()
                .dealType(req.dealType() != null ? req.dealType() : DealType.SALE)
                .contractNumber(req.contractNumber())
                .contractDate(req.contractDate())
                .commissionPercent(req.commissionPercent())
                .build();
    }

    private ParkingEntity buildParking(ListingRequest req) {
        if (req.parkingType() == null && req.parkingTotalSpaces() == null && req.parkingAvailableSpaces() == null
                && req.parkingHourlyRate() == null && req.parkingMonthlyRate() == null) return null;
        return ParkingEntity.builder()
                .parkingType(req.parkingType() != null ? req.parkingType() : Parking.NONE)
                .totalSpaces(req.parkingTotalSpaces())
                .availableSpaces(req.parkingAvailableSpaces())
                .hourlyRate(req.parkingHourlyRate())
                .monthlyRate(req.parkingMonthlyRate())
                .build();
    }

    private ReadinessEntity buildReadiness(ListingRequest req) {
        if (req.readinessType() == null && req.expectedCompletion() == null && req.actualCompletion() == null
                && req.currentStage() == null && req.completionPercent() == null) return null;
        return ReadinessEntity.builder()
                .readinessType(req.readinessType() != null ? req.readinessType() : Readiness.IN_OPERATION)
                .expectedCompletion(req.expectedCompletion())
                .actualCompletion(req.actualCompletion())
                .currentStage(req.currentStage())
                .completionPercent(req.completionPercent())
                .build();
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
