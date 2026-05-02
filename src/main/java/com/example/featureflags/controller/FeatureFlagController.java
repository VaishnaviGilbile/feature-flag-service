package com.example.featureflags.controller;

import com.example.featureflags.dto.FlagDtos.*;
import com.example.featureflags.service.FeatureFlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService service;

    /**
     * GET /api/v1/flags
     * List all feature flags.
     */
    @GetMapping
    public ResponseEntity<List<FlagResponse>> getAllFlags() {
        return ResponseEntity.ok(service.getAllFlags());
    }

    /**
     * GET /api/v1/flags/{key}
     * Get a single flag by its key.
     */
    @GetMapping("/{key}")
    public ResponseEntity<FlagResponse> getFlag(@PathVariable String key) {
        return ResponseEntity.ok(service.getFlagByKey(key));
    }

    /**
     * POST /api/v1/flags
     * Create a new feature flag.
     */
    @PostMapping
    public ResponseEntity<FlagResponse> createFlag(@Valid @RequestBody CreateFlagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFlag(request));
    }

    /**
     * PATCH /api/v1/flags/{key}
     * Partially update a flag (only provided fields are updated).
     */
    @PatchMapping("/{key}")
    public ResponseEntity<FlagResponse> updateFlag(
            @PathVariable String key,
            @RequestBody UpdateFlagRequest request) {
        return ResponseEntity.ok(service.updateFlag(key, request));
    }

    /**
     * DELETE /api/v1/flags/{key}
     * Delete a flag.
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteFlag(@PathVariable String key) {
        service.deleteFlag(key);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/flags/{key}/evaluate
     * Evaluate whether a flag is on or off for a given user + environment.
     */
    @PostMapping("/{key}/evaluate")
    public ResponseEntity<EvaluateResponse> evaluateFlag(
            @PathVariable String key,
            @Valid @RequestBody EvaluateRequest request) {
        return ResponseEntity.ok(service.evaluateFlag(key, request));
    }
}
