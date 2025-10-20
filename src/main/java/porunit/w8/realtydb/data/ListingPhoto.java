package porunit.w8.realtydb.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "listing_photos",
       uniqueConstraints = {
         @UniqueConstraint(name="uq_listing_photo_order", columnNames = {"listing_id","position"})
       })
public class ListingPhoto {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Listing listing;


    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] data;


    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;     // image/jpeg, image/png ...

    @Column(name = "filename")
    private String filename;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "position", nullable = false)
    private int position;           // порядок (0..8). 0 можно считать «обложкой» по умолчанию

    @Column(name = "is_cover", nullable = false)
    private boolean cover;          // явная обложка

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
