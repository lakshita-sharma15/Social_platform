package com.example.platform.service;

import com.example.platform.entity.OAuthCredential;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.repository.OAuthCredentialRepository;
import com.example.platform.util.AesUtil;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialService {
    private final OAuthCredentialRepository repo;
    private final AesUtil aes;

    public CredentialService(OAuthCredentialRepository repo, AesUtil aes) { this.repo = repo; this.aes = aes; }

    /** Encrypt and store. Calling again for the same provider rotates the token and bumps the version. */
    @Transactional
    public OAuthCredential save(String owner, String provider, String accessToken, String refreshToken) {
        OAuthCredential c = repo.findByOwnerAndProvider(owner, provider).orElse(null);
        if (c == null) c = new OAuthCredential(owner, provider);
        else c.setTokenVersion(c.getTokenVersion() + 1);
        c.setAccessTokenEnc(aes.encrypt(accessToken));
        c.setRefreshTokenEnc(refreshToken == null ? null : aes.encrypt(refreshToken));
        c.setUpdatedAt(LocalDateTime.now());
        return repo.save(c);
    }

    @Transactional(readOnly = true)
    public OAuthCredential get(String owner, String provider) {
        return repo.findByOwnerAndProvider(owner, provider)
                .orElseThrow(() -> new ResourceNotFoundException("No credential for provider " + provider));
    }

    /** For internal use only (calling the third-party API); never returned to clients. */
    @Transactional(readOnly = true)
    public String decryptAccessToken(String owner, String provider) {
        return aes.decrypt(get(owner, provider).getAccessTokenEnc());
    }
}
