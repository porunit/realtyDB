package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.feed.AvitoFeed;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvitoFeedRepository extends JpaRepository<AvitoFeed, UUID> {

    @Query("select f from AvitoFeed f where f.id = :id")
    Optional<AvitoFeed> findById(@Param("id") UUID id);

    @Query("select f from AvitoFeed f order by f.createdAt desc")
    List<AvitoFeed> findAll();

    @Query("select case when count(f) > 0 then true else false end from AvitoFeed f where f.id = :id")
    boolean existsById(@Param("id") UUID id);
}
