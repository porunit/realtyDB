package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.ParkingEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingRepository extends JpaRepository<ParkingEntity, UUID> {

    @Query("select p from ParkingEntity p where p.id = :id")
    Optional<ParkingEntity> findById(@Param("id") UUID id);

    @Query("select p from ParkingEntity p order by p.id asc")
    List<ParkingEntity> findAll();
}
