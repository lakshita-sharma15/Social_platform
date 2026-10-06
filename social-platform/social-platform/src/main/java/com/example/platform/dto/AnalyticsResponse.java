package com.example.platform.dto;

import java.time.LocalDateTime;

public record AnalyticsResponse(long totalPosts, long totalLikes, double averageLikes, LocalDateTime computedAt) {}
