package com.example.featureflags.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// ─────────────────────────────────────────────
// Request DTOs
// ─────────────────────────────────────────────

public class FlagDtos {

    @Data
    public static class CreateFlagRequest {
        @NotBlank(message = "Key is required")
        @Pattern(regexp = "^[a-z0-9_-]+$", message = "Key must be lowercase alphanumeric with underscores/hyphens")
        private String key;

        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        private boolean enabled = false;

        @Min(0) @Max(100)
        private int rolloutPercentage = 100;

        private String environments;
        private String allowlist;
    }

    @Data
    public static class UpdateFlagRequest {
        private String name;
        private String description;
        private Boolean enabled;

        @Min(0) @Max(100)
        private Integer rolloutPercentage;

        private String environments;
        private String allowlist;
    }

    @Data
    public static class EvaluateRequest {
        @NotBlank(message = "userId is required")
        private String userId;

        private String environment;
    }

    // ─────────────────────────────────────────────
    // Response DTOs
    // ─────────────────────────────────────────────

    @Data
    public static class FlagResponse {
        private Long id;
        private String key;
        private String name;
        private String description;
        private boolean enabled;
        private int rolloutPercentage;
        private String environments;
        private String allowlist;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    public static class EvaluateResponse {
        private String flagKey;
        private String userId;
        private String environment;
        private boolean enabled;
        private String reason;
    }

    @Data
    public static class ErrorResponse {
        private int status;
        private String message;
        private String timestamp;
    }
}
