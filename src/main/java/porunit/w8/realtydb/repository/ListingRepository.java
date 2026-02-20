package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @Query("select l from Listing l where l.createdBy = :createdBy order by l.createdAt desc")
    List<Listing> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy);

    @Query("select l from Listing l where l.id = :id")
    Optional<Listing> findById(@Param("id") UUID id);

    @Query("select l from Listing l order by l.createdAt desc")
    List<Listing> findAll();

    @Query("select case when count(l) > 0 then true else false end from Listing l where l.id = :id")
    boolean existsById(@Param("id") UUID id);
}
