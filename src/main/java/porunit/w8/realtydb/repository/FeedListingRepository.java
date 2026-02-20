package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.feed.FeedListing;

import java.util.List;
import java.util.UUID;

public interface FeedListingRepository extends JpaRepository<FeedListing, FeedListing.FeedListingId> {

    @Query("select fl from FeedListing fl where fl.feed.id = :feedId order by fl.addedAt asc")
    List<FeedListing> findByFeed_Id(@Param("feedId") UUID feedId);

    @Query("select count(fl) from FeedListing fl where fl.feed.id = :feedId")
    long countByFeed_Id(@Param("feedId") UUID feedId);
}
