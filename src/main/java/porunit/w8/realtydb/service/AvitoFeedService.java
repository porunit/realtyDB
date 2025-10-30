package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import porunit.w8.realtydb.components.AvitoFeedBusinessValidator;
import porunit.w8.realtydb.components.AvitoXmlWriter;
import porunit.w8.realtydb.data.domain.*;
import porunit.w8.realtydb.data.domain.feed.*;
import porunit.w8.realtydb.repository.AvitoFeedItemRepository;
import porunit.w8.realtydb.repository.AvitoFeedRepository;
import porunit.w8.realtydb.repository.ListingRepository;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvitoFeedService {

    private final ListingRepository listingRepository;
    private final AvitoFeedRepository feedRepository;
    private final AvitoFeedItemRepository feedItemRepository;
    private final AvitoFeedBusinessValidator validator;
    private final AvitoXmlWriter writer;

    public AvitoFeedCreateResponse createFeed(AvitoFeedCreateRequest req) throws Exception {
        // 1. Достать листинги
        List<Listing> listings = listingRepository.findAllById(req.listingIds());
        if (listings.size() != req.listingIds().size()) {
            // кто-то не найден
            Set<UUID> found = listings.stream().map(Listing::getId).collect(Collectors.toSet());
            for (UUID id : req.listingIds()) {
                if (!found.contains(id)) {
                    throw new EntityNotFoundException("Listing not found: " + id);
                }
            }
        }

        // 2. Бизнес-валидация
        var check = validator.validate(listings, req.purpose());
        if (!check.valid()) {
            throw new IllegalArgumentException("Feed validation failed: " + String.join("; ", check.errors()));
        }

        // 3. Генерация XML
        String xml = writer.generateXml(listings, req.purpose());

        // 4. Сохранить AvitoFeed
        AvitoFeed feed = new AvitoFeed();
        feed.setPurpose(req.purpose());
        feed.setXmlPayload(xml);

        feed = feedRepository.save(feed);

        // 5. Сохранить AvitoFeedItem связи
        List<AvitoFeedItem> items = new ArrayList<>();
        for (Listing l : listings) {
            AvitoFeedItem it = AvitoFeedItem.builder()
                    .feed(feed)
                    .listingId(l.getId())
                    .build();
            items.add(it);
        }
        feedItemRepository.saveAll(items);

        feed.setItems(items); // чтобы в ответе был доступен список

        return new AvitoFeedCreateResponse(
                feed.getId(),
                xmlUrl(feed.getId()),
                feed.getCreatedAt(),
                feed.getPurpose().name(),
                items.stream().map(AvitoFeedItem::getListingId).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<AvitoFeedListItemDto> listFeeds() {
        var feeds = feedRepository.findAll().stream()
                .sorted(Comparator.comparing(AvitoFeed::getCreatedAt).reversed())
                .toList();

        // грузим itemCount
        Map<UUID, Long> counts = feedItemRepository.findAll().stream()
                .collect(Collectors.groupingBy(it -> it.getFeed().getId(), Collectors.counting()));

        List<AvitoFeedListItemDto> out = new ArrayList<>();

        for (AvitoFeed f : feeds) {
            long cnt = counts.getOrDefault(f.getId(), 0L);
            out.add(new AvitoFeedListItemDto(
                    f.getId(),
                    f.getCreatedAt(),
                    f.getPurpose().name(),
                    (int) cnt,
                    xmlUrl(f.getId())
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public String getXml(UUID feedId) {
        AvitoFeed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found: " + feedId));
        return feed.getXmlPayload();
    }

    public void deleteFeed(UUID feedId) {
        if (!feedRepository.existsById(feedId)) {
            throw new EntityNotFoundException("Feed not found: " + feedId);
        }
        feedRepository.deleteById(feedId);
        // благодаря orphanRemoval = true на AvitoFeed.items, AvitoFeedItem строки тоже удалятся
    }

    private String xmlUrl(UUID feedId) {
        return "/api/feeds/avito/" + feedId + ".xml";
    }
}
