package com.example.platform.dto;

import java.time.LocalDateTime;

/** Standard envelope used by every endpoint (success and error). */
public class ApiResponse<T> {
    private final String status;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(String status, String message, T data) {
        this.status = status; this.message = message; this.data = data;
    }
    public static <T> ApiResponse<T> success(String message, T data) { return new ApiResponse<>("success", message, data); }
    public static <T> ApiResponse<T> error(String message, T data) { return new ApiResponse<>("error", message, data); }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
