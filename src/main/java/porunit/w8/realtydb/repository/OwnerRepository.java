package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.Owner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {

    @Query("select o from Owner o where o.id = :id")
    Optional<Owner> findById(@Param("id") UUID id);

    @Query("select o from Owner o order by o.id asc")
    List<Owner> findAll();
}
