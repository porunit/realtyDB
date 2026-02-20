package porunit.w8.realtydb.data.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "building")
public class Building {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "building_type_id", nullable = false, length = 50)
    private BuildingType buildingType;

    @Column(name = "developer_name", length = 255)
    private String developerName;

    @Positive
    @Column(name = "floors_count")
    private Integer floorsCount;
}
