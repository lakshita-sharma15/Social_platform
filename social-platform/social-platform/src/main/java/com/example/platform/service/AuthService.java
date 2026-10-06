package com.example.platform.service;

import com.example.platform.dto.AuthDtos.*;
import com.example.platform.entity.RefreshToken;
import com.example.platform.entity.User;
import com.example.platform.repository.RefreshTokenRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.security.JwtUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;
    private final long refreshDays;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtUtil jwt,
                       @Value("${app.jwt.refresh-expiry-days}") long refreshDays) {
        this.users = users; this.refreshTokens = refreshTokens; this.encoder = encoder;
        this.authManager = authManager; this.jwt = jwt; this.refreshDays = refreshDays;
    }

    public long refreshMaxAgeSeconds() { return refreshDays * 24 * 3600; }

    @Transactional
    public void register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) throw new IllegalArgumentException("Username already taken");
        users.save(new User(req.username(), encoder.encode(req.password()), "ROLE_USER"));
    }

    /** @return [TokenResponse, refreshToken] */
    @Transactional
    public Object[] login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User u = users.findByUsername(req.username()).orElseThrow();
        return new Object[]{access(u), newRefresh(u.getUsername())};
    }

    /** Token rotation: the presented refresh token is consumed and a new pair is issued. */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public Object[] refresh(String token) {
        if (token == null) throw new BadCredentialsException("Missing refresh token");
        RefreshToken rt = refreshTokens.findByToken(token).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        refreshTokens.delete(rt);                       // old token invalidated -> replay fails
        if (rt.getExpiresAt().isBefore(Instant.now())) throw new BadCredentialsException("Refresh token expired");
        User u = users.findByUsername(rt.getUsername()).orElseThrow();
        return new Object[]{access(u), newRefresh(u.getUsername())};
    }

    @Transactional
    public void logout(String token) {
        if (token != null) refreshTokens.deleteByToken(token);
    }

    private TokenResponse access(User u) {
        return new TokenResponse(jwt.generateToken(u.getUsername(), u.getRole()), "Bearer", jwt.getAccessExpiryMs());
    }

    private String newRefresh(String username) {
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        refreshTokens.save(new RefreshToken(token, username, Instant.now().plus(refreshDays, ChronoUnit.DAYS)));
        return token;
    }
}
