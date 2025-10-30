// web/dto/ListingDto.java
package porunit.w8.realtydb.data;

import porunit.w8.realtydb.data.domain.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ListingDto(
    UUID id,
    String title,
    String description,
    BigDecimal price,
    String location,
    OwnershipType ownership,
    String email,
    String companyName,
    String phone,
    EntranceType entrance,
    Integer floor,
    Double area,
    Double ceilingHeight,
    Finishing finishing,
    Integer powerKw,
    Heating heating,
    Readiness readiness,
    BuildingType buildingType,
    RoadDistance roadDistance,
    Parking parking,
    DealType dealType,
    Boolean leased,
    String tenant,
    BigDecimal monthlyRent,
    Boolean agentCommission,
    Boolean vatIncluded,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<PhotoMetaDto> photos // только метаданные
) {}
