package com.microproject.linktoolkit.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microproject.linktoolkit.link.Link;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"passwordHash", "apiKeyHash", "links"}) // Exclude sensitive/large fields from toString
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore // IMPORTANT: Never serialize the password hash in API responses
    private String passwordHash;

    @Column(name = "api_key_hash", unique = true)
    @JsonIgnore // IMPORTANT: Never serialize the API key hash either
    private String apiKeyHash;

    @Column(name = "api_key_public_id", unique = true)
    private String apiKeyPublicId;

    // This defines the one-to-many relationship. One User can have many Links.
    // 'mappedBy = "user"' tells JPA that the 'user' field in the Link class owns this relationship.
    // 'cascade = CascadeType.ALL' means if a User is deleted, all their associated Links are also deleted.
    // 'fetch = FetchType.LAZY' is a performance optimization. Links are only loaded from the DB when explicitly requested.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Handles the "forward" part of the reference to prevent infinite recursion in JSON serialization
    private List<Link> links;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // --- UserDetails Implementation ---
    // These methods are required by Spring Security to handle authentication and authorization.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For this project, all registered users have the same role.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // Spring Security will use this method to get the stored password hash for comparison.
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        // We are using the email as the unique identifier for login.
        return this.email;
    }

    // For this project, we assume accounts are always active.
    // These can be extended later to handle account locking, expiry, etc.

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
