package com.example.platform.controller;

import com.example.platform.dto.AnalyticsResponse;
import com.example.platform.dto.ApiResponse;
import com.example.platform.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> analytics() {
        return ResponseEntity.ok(ApiResponse.success("Analytics (cached)", service.getAnalytics()));
    }
}
