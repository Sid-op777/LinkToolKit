package com.microproject.linktoolkit.url.repository;

import com.microproject.linktoolkit.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, String> {
    Optional<Url> findById(String shortUrl);
}