package porunit.w8.realtydb.data.domain.feed;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "avito_feed_items")
public class AvitoFeedItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private AvitoFeed feed;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;
}
