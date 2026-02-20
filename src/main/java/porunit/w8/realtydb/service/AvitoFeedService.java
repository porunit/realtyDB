package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.components.AvitoFeedBusinessValidator;
import porunit.w8.realtydb.components.AvitoXmlWriter;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.User;
import porunit.w8.realtydb.data.domain.feed.*;
import porunit.w8.realtydb.repository.AvitoFeedRepository;
import porunit.w8.realtydb.repository.FeedListingRepository;
import porunit.w8.realtydb.repository.ListingRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvitoFeedService {

    private final ListingRepository listingRepository;
    private final AvitoFeedRepository feedRepository;
    private final FeedListingRepository feedListingRepository;
    private final AvitoFeedBusinessValidator validator;
    private final AvitoXmlWriter writer;
    private final UserService userService;

    public AvitoFeedCreateResponse createFeed(AvitoFeedCreateRequest req) throws Exception {
        List<Listing> listings = listingRepository.findAllById(req.listingIds());
        if (listings.size() != req.listingIds().size()) {
            Set<UUID> found = listings.stream().map(Listing::getId).collect(Collectors.toSet());
            for (UUID id : req.listingIds()) {
                if (!found.contains(id)) {
                    throw new EntityNotFoundException("Listing not found: " + id);
                }
            }
        }

        var check = validator.validate(listings, req.purpose());
        if (!check.valid()) {
            throw new IllegalArgumentException("Feed validation failed: " + String.join("; ", check.errors()));
        }

        String xml = writer.generateXml(listings, req.purpose());

        User currentUser = userService.requireCurrentUser();
        if (currentUser.getId() == null) {
            throw new IllegalStateException("Config admin cannot create feeds. Register an admin user and use it.");
        }
        AvitoFeed feed = new AvitoFeed();
        feed.setPurpose(req.purpose());
        feed.setXmlPayload(xml);
        feed.setCreatedBy(currentUser);
        feed = feedRepository.save(feed);

        for (Listing l : listings) {
            FeedListing fl = FeedListing.builder()
                    .feed(feed)
                    .listing(l)
                    .build();
            feedListingRepository.save(fl);
        }

        List<UUID> listingIds = listings.stream().map(Listing::getId).toList();
        return new AvitoFeedCreateResponse(
                feed.getId(),
                xmlUrl(feed.getId()),
                feed.getCreatedAt(),
                feed.getPurpose().name(),
                listingIds
        );
    }

    @Transactional(readOnly = true)
    public List<AvitoFeedListItemDto> listFeeds() {
        var feeds = feedRepository.findAll().stream()
                .sorted(Comparator.comparing(AvitoFeed::getCreatedAt).reversed())
                .toList();

        List<AvitoFeedListItemDto> out = new ArrayList<>();
        for (AvitoFeed f : feeds) {
            int cnt = (int) feedListingRepository.countByFeed_Id(f.getId());
            out.add(new AvitoFeedListItemDto(
                    f.getId(),
                    f.getCreatedAt(),
                    f.getPurpose().name(),
                    cnt,
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
    }

    private String xmlUrl(UUID feedId) {
        return "/api/feeds/avito/" + feedId + ".xml";
    }
}
