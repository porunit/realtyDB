package porunit.w8.realtydb.data.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "parking")
public class ParkingEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type_id", nullable = false, length = 20)
    private Parking parkingType;

    @Min(0)
    @Column(name = "total_spaces")
    private Integer totalSpaces;

    @Min(0)
    @Column(name = "available_spaces")
    private Integer availableSpaces;

    @DecimalMin("0")
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @DecimalMin("0")
    @Column(name = "monthly_rate", precision = 10, scale = 2)
    private BigDecimal monthlyRate;
}
