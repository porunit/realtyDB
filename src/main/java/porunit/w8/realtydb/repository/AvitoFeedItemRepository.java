package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import porunit.w8.realtydb.data.domain.feed.AvitoFeedItem;

import java.util.List;
import java.util.UUID;

public interface AvitoFeedItemRepository extends JpaRepository<AvitoFeedItem, UUID> {

    List<AvitoFeedItem> findAllByFeed_Id(UUID feedId);
}
