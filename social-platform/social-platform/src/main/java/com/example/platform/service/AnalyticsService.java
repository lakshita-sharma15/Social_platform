package com.example.platform.service;

import com.example.platform.dto.AnalyticsResponse;
import com.example.platform.entity.Post;
import com.example.platform.repository.PostRepository;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final PostRepository posts;

    public AnalyticsService(PostRepository posts) { this.posts = posts; }

    /** First call hits the DB; later calls within the TTL are served from Ehcache (computedAt stays the same). */
    @Cacheable("analytics")
    public AnalyticsResponse getAnalytics() {
        long total = posts.countByStatus(Post.PUBLISHED);
        long likes = posts.sumLikes();
        return new AnalyticsResponse(total, likes, total == 0 ? 0 : (double) likes / total, LocalDateTime.now());
    }
}
