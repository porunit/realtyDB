package porunit.w8.realtydb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import porunit.w8.realtydb.data.domain.Deal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    @Query("select d from Deal d where d.id = :id")
    Optional<Deal> findById(@Param("id") UUID id);

    @Query("select d from Deal d order by d.id asc")
    List<Deal> findAll();
}
