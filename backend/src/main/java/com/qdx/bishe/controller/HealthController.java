package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "UP");
        payload.put("time", LocalDateTime.now());
        return ApiResponse.success(payload);
    }
}

