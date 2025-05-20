package com.microproject.linktoolkit.url.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "urls")
@Data //getters and setters through lombok
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    private String id; // Will store the short URL code directly as the ID

    @Column(nullable = false, length = 2048)
    private String longUrl;

    private Instant createdAt;
    private Instant expiresAt;
    private Instant lastVisited;

    @Column(name = "user_id")
    private Long userId; // temporary for now (no auth yet)

}

//@Document(collection = "urls")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Url {
//
//    @Id
//    private String id; // Will store the short URL code directly as the ID
//
//    private String longUrl;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Instant createdAt;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Instant expiresAt;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Instant lastVisited;
//
//    @Indexed
//    private Long userId;
//}