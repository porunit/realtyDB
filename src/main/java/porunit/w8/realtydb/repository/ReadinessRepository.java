package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.ReadinessEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadinessRepository extends JpaRepository<ReadinessEntity, UUID> {

    @Query("select r from ReadinessEntity r where r.id = :id")
    Optional<ReadinessEntity> findById(@Param("id") UUID id);

    @Query("select r from ReadinessEntity r order by r.id asc")
    List<ReadinessEntity> findAll();
}
