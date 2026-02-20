package porunit.w8.realtydb.data.domain;

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
@Table(name = "listing")
public class Listing {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Positive
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "location", length = 500)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Email
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "entrance_type_id", length = 20)
    private EntranceType entranceType;

    @Column(name = "floor")
    private Integer floor;

    @Positive
    @Column(name = "area")
    private Double area;

    @Positive
    @Column(name = "ceiling_height")
    private Double ceilingHeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "finishing_id", length = 20)
    private Finishing finishing;

    @Positive
    @Column(name = "power_kw")
    private Integer powerKw;

    @Enumerated(EnumType.STRING)
    @Column(name = "heating_id", length = 20)
    private Heating heating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "readiness_id")
    private ReadinessEntity readiness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Enumerated(EnumType.STRING)
    @Column(name = "road_distance_id", length = 30)
    private RoadDistance roadDistance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private ParkingEntity parking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @Column(name = "leased")
    private Boolean leased;

    @Column(name = "tenant", length = 255)
    private String tenant;

    @Column(name = "monthly_rent", precision = 15, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "agent_commission")
    private Boolean agentCommission;

    @Column(name = "vat_included")
    private Boolean vatIncluded;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, createdAt ASC")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"data", "listing"})
    private java.util.List<ListingPhoto> photos = new java.util.ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public OwnershipType getOwnership() {
        return owner != null ? owner.getOwnershipType() : null;
    }

    public EntranceType getEntrance() {
        return entranceType;
    }

    public BuildingType getBuildingType() {
        return building != null ? building.getBuildingType() : null;
    }

    public DealType getDealType() {
        return deal != null ? deal.getDealType() : null;
    }

    public Parking getParkingType() {
        return parking != null ? parking.getParkingType() : null;
    }

    public Readiness getReadinessType() {
        return readiness != null ? readiness.getReadinessType() : null;
    }
}
