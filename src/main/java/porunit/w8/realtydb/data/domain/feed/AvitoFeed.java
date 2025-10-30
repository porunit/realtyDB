package porunit.w8.realtydb.data.domain.feed;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "avito_feeds")
public class AvitoFeed {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "purpose", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedPurpose purpose; 
    // SALE / RENT — то есть "продажа" или "аренда" в терминах Авито

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Храним финальный XML (тот, который будем отдавать Авито)
    @Lob
    @Column(name = "xml_payload", columnDefinition = "text", nullable = false)
    private String xmlPayload;

    // Привязанные объявления (снимок на момент генерации)
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AvitoFeedItem> items;
}
