package com.example.platform.controller;

import com.example.platform.dto.ApiResponse;
import com.example.platform.dto.AuthDtos.*;
import com.example.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String COOKIE = "refreshToken";
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        return withCookie("Login successful", auth.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@CookieValue(name = COOKIE, required = false) String token) {
        return withCookie("Token refreshed", auth.refresh(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = COOKIE, required = false) String token) {
        auth.logout(token);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie("", 0).toString())
                .body(ApiResponse.success("Logged out", null));
    }

    private ResponseEntity<ApiResponse<TokenResponse>> withCookie(String msg, Object[] pair) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie((String) pair[1], auth.refreshMaxAgeSeconds()).toString())
                .body(ApiResponse.success(msg, (TokenResponse) pair[0]));
    }

    // HttpOnly: JavaScript cannot read it (XSS protection); set secure(true) behind HTTPS in production
    private ResponseCookie cookie(String value, long maxAge) {
        return ResponseCookie.from(COOKIE, value).httpOnly(true).secure(false).sameSite("Strict")
                .path("/api/auth").maxAge(maxAge).build();
    }
}
