package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.Building;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    @Query("select b from Building b where b.id = :id")
    Optional<Building> findById(@Param("id") UUID id);

    @Query("select b from Building b order by b.id asc")
    List<Building> findAll();
}
