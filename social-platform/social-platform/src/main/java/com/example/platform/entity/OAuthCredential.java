package com.example.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_credentials", uniqueConstraints = @UniqueConstraint(columnNames = {"owner", "provider"}))
public class OAuthCredential {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private String provider;
    @Column(length = 4000)
    private String accessTokenEnc;   // AES-GCM encrypted, never plaintext
    @Column(length = 4000)
    private String refreshTokenEnc;
    private int tokenVersion = 1;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public OAuthCredential() {}
    public OAuthCredential(String owner, String provider) { this.owner = owner; this.provider = provider; }
    public String getProvider() { return provider; }
    public String getAccessTokenEnc() { return accessTokenEnc; }
    public void setAccessTokenEnc(String v) { this.accessTokenEnc = v; }
    public String getRefreshTokenEnc() { return refreshTokenEnc; }
    public void setRefreshTokenEnc(String v) { this.refreshTokenEnc = v; }
    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int v) { this.tokenVersion = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}
