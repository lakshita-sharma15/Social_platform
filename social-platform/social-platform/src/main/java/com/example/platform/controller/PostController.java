package com.example.platform.controller;

import com.example.platform.dto.*;
import com.example.platform.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService service;

    public PostController(PostService service) { this.service = service; }

    // GET /api/posts?page=0&size=10&sort=likes,desc
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Posts fetched", service.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post fetched", service.get(id)));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> byAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(ApiResponse.success("Posts fetched", service.byAuthor(authorId)));
    }

    @GetMapping("/with-comments")
    public ResponseEntity<ApiResponse<List<PostResponse>>> withComments() {
        return ResponseEntity.ok(ApiResponse.success("Posts with comments (JOIN FETCH)", service.withComments()));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<PostResponse>>> top() {
        return ResponseEntity.ok(ApiResponse.success("Top posts (native query)", service.top()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody PostRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created", service.create(auth.getName(), req)));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<PostResponse>> schedule(@Valid @RequestBody SchedulePostRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post scheduled", service.schedule(auth.getName(), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> update(@PathVariable Long id, @Valid @RequestBody PostRequest req,
                                                            Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.success("Post updated", service.update(id, req, auth.getName(), admin)));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<PostResponse>> like(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post liked", service.like(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }
}
