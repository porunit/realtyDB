package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import porunit.w8.realtydb.data.domain.feed.AvitoFeed;

import java.util.UUID;

public interface AvitoFeedRepository extends JpaRepository<AvitoFeed, UUID> {
}
