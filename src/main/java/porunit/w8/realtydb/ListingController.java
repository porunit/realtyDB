package porunit.w8.realtydb;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.data.Listing;
import porunit.w8.realtydb.service.ListingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings")
@CrossOrigin("*")
public class ListingController {

    private final ListingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Listing create(@Valid @RequestBody Listing listing) {
        return service.create(listing);
    }

    @GetMapping
    public List<Listing> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Listing getOne(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public Listing update(@PathVariable UUID id, @Valid @RequestBody Listing listing) {
        return service.update(id, listing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
