package com.example.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "Content must not be empty")
        @Size(max = 280, message = "Content exceeds limit")
        String content) {}
