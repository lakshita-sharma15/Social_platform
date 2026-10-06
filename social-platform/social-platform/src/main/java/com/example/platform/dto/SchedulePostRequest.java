package com.example.platform.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record SchedulePostRequest(
        @NotBlank(message = "Content must not be empty")
        @Size(max = 280, message = "Content exceeds limit")
        String content,
        @NotNull(message = "scheduledAt is required")
        @Future(message = "scheduledAt must be in the future")
        LocalDateTime scheduledAt) {}
