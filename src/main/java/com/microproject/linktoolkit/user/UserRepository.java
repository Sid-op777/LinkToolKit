package com.microproject.linktoolkit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their email address.
     * Spring Data JPA automatically implements this method based on its name.
     * "findBy" is the keyword, and "Email" is the property to search by.
     *
     * @param email The email address to search for.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their hashed API key. This will be used for API authentication.
     *
     * @param apiKeyHash The hashed API key to search for.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    Optional<User> findByApiKeyHash(String apiKeyHash);

    Optional<User> findByApiKeyPublicId(String apiKeyPublicId);
}
