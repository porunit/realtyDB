package porunit.w8.realtydb;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import porunit.w8.realtydb.data.domain.feed.AvitoFeedCreateRequest;
import porunit.w8.realtydb.data.domain.feed.AvitoFeedCreateResponse;
import porunit.w8.realtydb.data.domain.feed.AvitoFeedListItemDto;
import porunit.w8.realtydb.service.AvitoFeedService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds/avito")
@CrossOrigin("*")
public class AvitoFeedController {

    private final AvitoFeedService service;

    @PostMapping
    public ResponseEntity<AvitoFeedCreateResponse> create(@Valid @RequestBody AvitoFeedCreateRequest req) throws Exception {
        var resp = service.createFeed(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public List<AvitoFeedListItemDto> list() {
        return service.listFeeds();
    }

    @GetMapping(value = "/{feedId}.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getXml(@PathVariable UUID feedId) {
        String xml = service.getXml(feedId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @DeleteMapping("/{feedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID feedId) {
        service.deleteFeed(feedId);
    }
}
