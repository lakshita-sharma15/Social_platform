package com.example.platform.repository;

import com.example.platform.entity.OAuthCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthCredentialRepository extends JpaRepository<OAuthCredential, Long> {
    Optional<OAuthCredential> findByOwnerAndProvider(String owner, String provider);
}
