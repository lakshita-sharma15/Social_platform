package com.example.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String token;
    private String username;
    private Instant expiresAt;

    public RefreshToken() {}
    public RefreshToken(String token, String username, Instant expiresAt) {
        this.token = token; this.username = username; this.expiresAt = expiresAt;
    }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public Instant getExpiresAt() { return expiresAt; }
}
