package porunit.w8.realtydb.data.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "readiness")
public class ReadinessEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "readiness_type_id", nullable = false, length = 30)
    private Readiness readinessType;

    @Column(name = "expected_completion")
    private LocalDate expectedCompletion;

    @Column(name = "actual_completion")
    private LocalDate actualCompletion;

    @Column(name = "current_stage", length = 255)
    private String currentStage;

    @Min(0) @Max(100)
    @Column(name = "completion_percent")
    private Integer completionPercent;
}
