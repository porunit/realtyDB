package porunit.w8.realtydb;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.data.ListingDto;
import porunit.w8.realtydb.data.ListingRequest;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.statistics.ListingStatisticsDto;
import porunit.w8.realtydb.service.ListingService;
import porunit.w8.realtydb.service.ListingStatisticsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService service;
    private final ListingStatisticsService statisticsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Listing create(@Valid @RequestBody ListingRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ListingDto> getAll() {
        return service.findAllDto();
    }

    @GetMapping("/{id}")
    public ListingDto getOne(@PathVariable UUID id) {
        return service.findDtoById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody ListingRequest request) {
        service.update(id, request);
        return ResponseEntity.ok().body("{}");
    }

    @GetMapping("/{id}/view")
    public ListingDto getOneView(@PathVariable UUID id) {
        return service.findDtoById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/{id}/statistics")
    public ListingStatisticsDto getStatistics(@PathVariable UUID id) {
        return statisticsService.getStatistics(id);
    }
}
