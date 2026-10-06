package com.example.platform.controller;

import com.example.platform.dto.ApiResponse;
import com.example.platform.dto.AuthDtos.CredentialRequest;
import com.example.platform.entity.OAuthCredential;
import com.example.platform.service.CredentialService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {
    private final CredentialService service;

    public CredentialController(CredentialService service) { this.service = service; }

    // PUT again for the same provider = token rotation (version increments)
    @PutMapping("/{provider}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> store(@PathVariable String provider,
                                                                  @Valid @RequestBody CredentialRequest req,
                                                                  Authentication auth) {
        OAuthCredential c = service.save(auth.getName(), provider, req.accessToken(), req.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Credential stored encrypted", view(c)));
    }

    // Returns only the ciphertext as stored - plaintext tokens are never sent back to clients
    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable String provider, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Credential fetched", view(service.get(auth.getName(), provider))));
    }

    private Map<String, Object> view(OAuthCredential c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("provider", c.getProvider());
        m.put("tokenVersion", c.getTokenVersion());
        m.put("encryptedAccessToken", c.getAccessTokenEnc());
        m.put("updatedAt", c.getUpdatedAt());
        return m;
    }
}
