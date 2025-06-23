package com.microproject.linktoolkit.link;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microproject.linktoolkit.analytics.Click;
import com.microproject.linktoolkit.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "clicks"})
@Entity
@Table(name = "links", indexes = {
        // We explicitly define indexes here for performance, matching our schema design.
        @Index(name = "idx_links_short_alias", columnList = "short_alias", unique = true),
        @Index(name = "idx_links_user_id", columnList = "user_id"),
        @Index(name = "idx_links_anonymous_session_id", columnList = "anonymous_session_id")
})
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "short_alias", nullable = false, unique = true, length = 50)
    private String shortAlias;

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    // This is the "many" side of the many-to-one relationship with User.
    // 'fetch = FetchType.LAZY' means the User object is not loaded from the DB unless accessed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Specifies the foreign key column in the 'links' table
    @JsonBackReference // The "back" part of the reference, prevents serialization loops
    private User user;

    @Column(name = "anonymous_session_id")
    private UUID anonymousSessionId;

    @Column(name = "qr_code_path", length = 512)
    private String qrCodePath;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // One Link can have many Clicks. If a link is deleted, all its click records are deleted too.
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Click> clicks;

}
