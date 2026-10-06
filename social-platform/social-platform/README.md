# Social Platform – Combined Spring Boot Project (Experiments 5, 6, 7)

Java 17, Spring Boot 3.3, Spring Security 6, JPA + H2 (in-memory), Ehcache (JCache), JJWT.

## Run
```
mvn spring-boot:run
```
Demo users: `admin / admin123` (ROLE_ADMIN), `user / user123` (ROLE_USER). 25 posts are seeded.

## Where each experiment lives
| Experiment | Files |
|---|---|
| 5.1 REST design, validation, standard response, CORS | `PostController`, `PostRequest`, `ApiResponse`, `SecurityConfig#corsConfigurationSource` |
| 5.2 Exception handling, logging, correlation ID | `GlobalExceptionHandler`, `LoggingFilter`, `CorrelationInterceptor`, `WebConfig` |
| 6.1 Pagination & sorting | `PostController#list`, `PageResponse` |
| 6.2 JOIN FETCH, Ehcache, native query | `PostRepository`, `AnalyticsService`, `ehcache.xml` |
| 7.1 JWT + RBAC | `JwtUtil`, `JwtAuthFilter`, `SecurityConfig`, `@PreAuthorize` in `AdminController`/`PostController` |
| 7.2 AES, access/refresh tokens, rotation, HttpOnly cookie | `AesUtil`, `CredentialService`, `AuthService`, `AuthController` |

## Try it
```bash
# login (refresh token arrives as an HttpOnly cookie)
curl -i -c cj.txt -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"admin123"}'
TOKEN=<accessToken from response>

# paginated + sorted
curl -H "Authorization: Bearer $TOKEN" "localhost:8080/api/posts?page=0&size=5&sort=likes,desc"

# validation error (400 with field messages)
curl -X POST localhost:8080/api/posts -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"content":""}'

# schedule a post
curl -X POST localhost:8080/api/posts/schedule -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d '{"content":"later","scheduledAt":"2030-01-01T10:00:00"}'

# cached analytics: call twice, computedAt stays the same until cache expiry or a post change
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/analytics

# N+1 fix / native query
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/posts/with-comments
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/posts/top

# RBAC: 200 for admin, 403 for user, 401 without token
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/admin

# token rotation: reusing the old cookie a second time fails with 401
curl -b cj.txt -c cj.txt -X POST localhost:8080/api/auth/refresh

# encrypted credential storage (response shows ciphertext only; PUT again = rotation, version++)
curl -X PUT localhost:8080/api/credentials/google -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d '{"accessToken":"ya29.demo","refreshToken":"1//demo"}'
```

## Assignment checklist
- Exp 5: CRUD + schedule endpoint, Bean Validation, logging filter, global handler, correlation ID (`X-Correlation-Id` header and log lines).
- Exp 6: paginated GET /posts, N+1 fix (`/with-comments`; set `spring.jpa.show-sql=true` to compare), cached analytics, native top-posts query. For benchmarking, run JMeter against `/api/analytics` with and without `@Cacheable`.
- Exp 7: JWT filter, secured endpoints, `@PreAuthorize`, access + refresh tokens with rotation, AES-GCM encrypted OAuth tokens.

## Notes
- Move `app.jwt.secret` and `app.security.aes-key` to environment variables (`JWT_SECRET`, `AES_KEY`, Base64, 32 bytes) before real use; the defaults are demo-only.
- Set the cookie `secure(true)` when serving over HTTPS.
- Differences from the handouts: Security 6 DSL (`csrf(c -> c.disable())`), AES-GCM instead of bare `AES` (ECB), and `@PreAuthorize` denials mapped to 403 in the global handler.
