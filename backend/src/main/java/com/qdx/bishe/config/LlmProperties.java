package com.qdx.bishe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.llm")
public class LlmProperties {

    private String provider = "mock";
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature = 0.7D;
    private Boolean enableThinking = false;
    private Integer connectTimeoutMs = 5000;
    private Integer readTimeoutMs = 30000;
    private List<String> availableModels = new ArrayList<>();
}
