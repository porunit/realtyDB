// porunit/w8/realtydb/ListingController.java
package porunit.w8.realtydb;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.ListingDto;
import porunit.w8.realtydb.service.ListingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Listing create(@Valid @RequestBody Listing listing) {
        return service.create(listing);
    }

    // ВАЖНО: теперь отдаём DTO + метаданные фото (тонко)
    @GetMapping
    public List<ListingDto> getAll() {
        return service.findAllDto();
    }

    // Если хочешь, чтобы одиночный тоже был DTO:
    @GetMapping("/{id}")
    public ListingDto getOne(@PathVariable UUID id) {
        return service.findDtoById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable UUID id, @Valid @RequestBody Listing listing) {
        service.update(id, listing);
        return ResponseEntity.ok().body("{}");
    }

    // Доп. «view» можно оставить, но он уже дублирует getOne()
    @GetMapping("/{id}/view")
    public ListingDto getOneView(@PathVariable UUID id) {
        return service.findDtoById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
