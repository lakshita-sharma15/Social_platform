package com.example.platform.service;

import com.example.platform.dto.*;
import com.example.platform.entity.Post;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.repository.PostRepository;
import com.example.platform.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final PostRepository posts;
    private final UserRepository users;

    public PostService(PostRepository posts, UserRepository users) {
        this.posts = posts; this.users = users;
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public PostResponse create(String username, PostRequest req) {
        Post p = new Post();
        p.setContent(req.content());
        p.setAuthor(users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        Post saved = posts.save(p);
        log.info("Created post id={} by {}", saved.getId(), username);
        return PostResponse.from(saved, false);
    }

    @Transactional
    public PostResponse schedule(String username, SchedulePostRequest req) {
        Post p = new Post();
        p.setContent(req.content());
        p.setStatus(Post.SCHEDULED);
        p.setScheduledAt(req.scheduledAt());
        p.setAuthor(users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        return PostResponse.from(posts.save(p), false);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> list(Pageable pageable) {
        return PageResponse.of(posts.findByStatusWithAuthor(Post.PUBLISHED, pageable)
                .map(p -> PostResponse.from(p, false)));
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        log.info("Processing request for Post ID: {}", id);
        return PostResponse.from(find(id), false);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> byAuthor(Long authorId) {
        return posts.findByAuthorId(authorId).stream().map(p -> PostResponse.from(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> withComments() {
        return posts.findAllWithComments().stream().map(p -> PostResponse.from(p, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> top() {
        return posts.findTopPosts().stream().map(p -> PostResponse.from(p, false)).toList();
    }

    @Transactional
    public PostResponse update(Long id, PostRequest req, String username, boolean admin) {
        Post p = find(id);
        if (!admin && !p.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("Only the author or an admin can edit this post");
        }
        p.setContent(req.content());
        return PostResponse.from(p, false);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public PostResponse like(Long id) {
        Post p = find(id);
        p.setLikes(p.getLikes() + 1);
        return PostResponse.from(p, false);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public void delete(Long id) {
        posts.delete(find(id));
    }

    /** Publishes scheduled posts whose time has come. */
    @Scheduled(fixedRate = 30_000)
    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public void publishDue() {
        posts.findDue(LocalDateTime.now()).forEach(p -> {
            p.setStatus(Post.PUBLISHED);
            p.setCreatedAt(LocalDateTime.now());
            log.info("Published scheduled post id={}", p.getId());
        });
    }

    private Post find(Long id) {
        return posts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post " + id + " not found"));
    }
}
