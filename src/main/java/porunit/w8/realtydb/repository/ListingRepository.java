package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import porunit.w8.realtydb.data.domain.Listing;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
}
