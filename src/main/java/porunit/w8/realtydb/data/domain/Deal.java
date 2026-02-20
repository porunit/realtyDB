package porunit.w8.realtydb.data.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "deal")
public class Deal {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type_id", nullable = false, length = 30)
    private DealType dealType;

    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    @Column(name = "contract_date")
    private java.time.LocalDate contractDate;

    @DecimalMin("0") @DecimalMax("100")
    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent;
}
