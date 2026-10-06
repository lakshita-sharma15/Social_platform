package com.example.platform.dto;

import com.example.platform.entity.Post;
import java.time.LocalDateTime;

public record PostResponse(Long id, String content, int likes, String status,
                           LocalDateTime createdAt, LocalDateTime scheduledAt,
                           String author, Integer commentCount) {

    public static PostResponse from(Post p, boolean withComments) {
        return new PostResponse(p.getId(), p.getContent(), p.getLikes(), p.getStatus(),
                p.getCreatedAt(), p.getScheduledAt(), p.getAuthor().getUsername(),
                withComments ? p.getComments().size() : null);
    }
}
