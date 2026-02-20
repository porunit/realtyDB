package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import porunit.w8.realtydb.data.domain.Listing;
import porunit.w8.realtydb.data.domain.ListingPhoto;
import porunit.w8.realtydb.data.PhotoMetaDto;
import porunit.w8.realtydb.data.PhotoMetaWithListingDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, UUID> {

    @Query("select p from ListingPhoto p where p.listing = :listing order by p.position asc, p.createdAt asc")
    List<ListingPhoto> findByListingOrderByPositionAscCreatedAtAsc(@Param("listing") Listing listing);

    @Query("select count(p) from ListingPhoto p where p.listing = :listing")
    long countByListing(@Param("listing") Listing listing);

    @Query("select p from ListingPhoto p where p.id = :id and p.listing = :listing")
    Optional<ListingPhoto> findByIdAndListing(@Param("id") UUID id, @Param("listing") Listing listing);

    @Query("""
      select new porunit.w8.realtydb.data.PhotoMetaDto(
        p.id, p.filename, p.contentType, p.sizeBytes, p.position, p.cover
      )
      from ListingPhoto p
      where p.listing.id = :listingId
      order by p.position asc, p.createdAt asc
    """)
    List<PhotoMetaDto> findMetaByListingId(@Param("listingId") UUID listingId);

    @Query("""
      select new porunit.w8.realtydb.data.PhotoMetaWithListingDto(
        p.listing.id, p.id, p.filename, p.contentType, p.sizeBytes, p.position, p.cover
      )
      from ListingPhoto p
      where p.listing.id in :listingIds
      order by p.listing.id asc, p.position asc, p.createdAt asc
    """)
    List<PhotoMetaWithListingDto> findMetaByListingIdIn(@Param("listingIds") Collection<UUID> listingIds);

    @Query("select p from ListingPhoto p where p.id = :id")
    Optional<ListingPhoto> findById(@Param("id") UUID id);
}
