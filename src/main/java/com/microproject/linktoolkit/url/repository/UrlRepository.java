package com.microproject.linktoolkit.url.repository;

import com.microproject.linktoolkit.url.entity.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {
    Optional<Url> findById(String shortUrl);
}