package porunit.w8.realtydb.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String title;              // 1. Название

    @Column(columnDefinition = "text")
    private String description;        // 2. Описание

    @Positive
    private BigDecimal price;          // 3. Цена

    private String location;           // 4. Расположение

    @Enumerated(EnumType.STRING)
    private OwnershipType ownership;   // 5. Право собственности

    @Email
    private String email;              // 6. Почта

    private String companyName;        // 7. Название компании

    private String phone;              // 8. Телефон

    @Enumerated(EnumType.STRING)
    private EntranceType entrance;     // 9. Вход

    private Integer floor;             // 10. Этаж

    @Positive
    private Double area;               // 11. Площадь (м²)

    @Positive
    private Double ceilingHeight;      // 12. Высота потолков (м)

    @Enumerated(EnumType.STRING)
    private Finishing finishing;       // 13. Отделка

    @Positive
    private Integer powerKw;           // 14. Мощность электросети (кВт)

    @Enumerated(EnumType.STRING)
    private Heating heating;           // 15. Отопление

    @Enumerated(EnumType.STRING)
    private Readiness readiness;       // 16. Готовность

    @Enumerated(EnumType.STRING)
    private BuildingType buildingType; // 17. Тип здания

    @Enumerated(EnumType.STRING)
    private RoadDistance roadDistance; // 18. Удаленность от дороги

    @Enumerated(EnumType.STRING)
    private Parking parking;           // 19. Парковка

    @Enumerated(EnumType.STRING)
    private DealType dealType;         // 20. Тип сделки

    private Boolean leased;            // 21. Помещение сдано (да/нет)

    private String tenant;             // если сдано: арендатор
    private BigDecimal monthlyRent;    // если сдано: месячный платеж

    private Boolean agentCommission;   // 22. Комиссия агенту (да/нет)
    private Boolean vatIncluded;       // 23. НДС включён (да/нет)

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, createdAt ASC")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"data", "listing"})
    private java.util.List<ListingPhoto> photos = new java.util.ArrayList<>();


    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
