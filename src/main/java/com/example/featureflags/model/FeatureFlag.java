package com.example.featureflags.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "feature_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "flag_key", unique = true, nullable = false)
    private String key;

    @NotBlank
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean enabled = false;

    /**
     * Rollout percentage (0-100).
     * e.g. 50 means 50% of users will see this feature enabled.
     */
    @Column(nullable = false)
    private int rolloutPercentage = 100;

    /**
     * Comma-separated list of environments (e.g. "production,staging").
     * Empty means available in all environments.
     */
    private String environments;

    /**
     * Comma-separated list of user IDs that always get this flag enabled
     * (useful for beta testers, internal users).
     */
    @Column(columnDefinition = "TEXT")
    private String allowlist;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the allowlist as a Set of user IDs.
     */
    public Set<String> getAllowlistSet() {
        if (allowlist == null || allowlist.isBlank()) return new HashSet<>();
        Set<String> result = new HashSet<>();
        for (String id : allowlist.split(",")) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    /**
     * Returns the environments as a Set.
     */
    public Set<String> getEnvironmentSet() {
        if (environments == null || environments.isBlank()) return new HashSet<>();
        Set<String> result = new HashSet<>();
        for (String env : environments.split(",")) {
            String trimmed = env.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}