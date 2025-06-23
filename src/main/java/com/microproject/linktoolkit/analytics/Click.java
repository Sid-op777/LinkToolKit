package com.microproject.linktoolkit.analytics;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.microproject.linktoolkit.link.Link;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "link")
@Entity
@Table(name = "clicks", indexes = {
        // This index is critical for performance when querying analytics for a specific link.
        @Index(name = "idx_clicks_link_id", columnList = "link_id")
})
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // This defines the "many" side of the many-to-one relationship with Link.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", nullable = false) // A click MUST belong to a link.
    @JsonBackReference // The "back" part of the Link->Click relationship.
    private Link link;

    @CreationTimestamp
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referer", columnDefinition = "TEXT")
    private String referer;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "device_type", length = 20)
    private String deviceType;
}
