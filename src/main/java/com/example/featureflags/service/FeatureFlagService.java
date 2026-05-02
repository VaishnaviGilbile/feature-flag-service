package com.example.featureflags.service;

import com.example.featureflags.dto.FlagDtos.*;
import com.example.featureflags.exception.FlagNotFoundException;
import com.example.featureflags.exception.DuplicateFlagException;
import com.example.featureflags.model.FeatureFlag;
import com.example.featureflags.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FeatureFlagRepository repository;

    // ─────────────────────────────────────────────
    // CRUD Operations
    // ─────────────────────────────────────────────

    public List<FlagResponse> getAllFlags() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FlagResponse getFlagByKey(String key) {
        return toResponse(findByKeyOrThrow(key));
    }

    @Transactional
    public FlagResponse createFlag(CreateFlagRequest request) {
        if (repository.existsByKey(request.getKey())) {
            throw new DuplicateFlagException("Flag with key '" + request.getKey() + "' already exists");
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setKey(request.getKey());
        flag.setName(request.getName());
        flag.setDescription(request.getDescription());
        flag.setEnabled(request.isEnabled());
        flag.setRolloutPercentage(request.getRolloutPercentage());
        flag.setEnvironments(request.getEnvironments());
        flag.setAllowlist(request.getAllowlist());

        return toResponse(repository.save(flag));
    }

    @Transactional
    public FlagResponse updateFlag(String key, UpdateFlagRequest request) {
        FeatureFlag flag = findByKeyOrThrow(key);

        if (request.getName() != null)              flag.setName(request.getName());
        if (request.getDescription() != null)       flag.setDescription(request.getDescription());
        if (request.getEnabled() != null)           flag.setEnabled(request.getEnabled());
        if (request.getRolloutPercentage() != null) flag.setRolloutPercentage(request.getRolloutPercentage());
        if (request.getEnvironments() != null)      flag.setEnvironments(request.getEnvironments());
        if (request.getAllowlist() != null)          flag.setAllowlist(request.getAllowlist());

        return toResponse(repository.save(flag));
    }

    @Transactional
    public void deleteFlag(String key) {
        FeatureFlag flag = findByKeyOrThrow(key);
        repository.delete(flag);
    }

    // ─────────────────────────────────────────────
    // Evaluation Logic
    // ─────────────────────────────────────────────

    /**
     * Evaluates whether a flag is enabled for a given user + environment.
     *
     * Evaluation order:
     *  1. Flag doesn't exist → disabled
     *  2. Flag globally disabled → disabled
     *  3. User in allowlist → enabled (bypass rollout)
     *  4. Environment doesn't match → disabled
     *  5. Rollout percentage check (deterministic hash) → enabled/disabled
     */
    public EvaluateResponse evaluateFlag(String key, EvaluateRequest request) {
        EvaluateResponse response = new EvaluateResponse();
        response.setFlagKey(key);
        response.setUserId(request.getUserId());
        response.setEnvironment(request.getEnvironment());

        FeatureFlag flag = repository.findByKey(key).orElse(null);

        if (flag == null) {
            response.setEnabled(false);
            response.setReason("Flag not found");
            return response;
        }

        if (!flag.isEnabled()) {
            response.setEnabled(false);
            response.setReason("Flag is globally disabled");
            return response;
        }

        // Allowlist check: these users always get the flag
        Set<String> allowlist = flag.getAllowlistSet();
        if (!allowlist.isEmpty() && allowlist.contains(request.getUserId())) {
            response.setEnabled(true);
            response.setReason("User is in allowlist");
            return response;
        }

        // Environment check
        Set<String> envs = flag.getEnvironmentSet();
        if (!envs.isEmpty() && request.getEnvironment() != null
                && !envs.contains(request.getEnvironment())) {
            response.setEnabled(false);
            response.setReason("Flag not enabled for environment: " + request.getEnvironment());
            return response;
        }

        // Rollout percentage check (deterministic: same user always gets same result)
        int rollout = flag.getRolloutPercentage();
        if (rollout >= 100) {
            response.setEnabled(true);
            response.setReason("100% rollout");
            return response;
        }
        if (rollout <= 0) {
            response.setEnabled(false);
            response.setReason("0% rollout");
            return response;
        }

        // Deterministic bucketing using hash of (flagKey + userId)
        int bucket = Math.abs((key + ":" + request.getUserId()).hashCode()) % 100;
        boolean inRollout = bucket < rollout;
        response.setEnabled(inRollout);
        response.setReason(inRollout
                ? "User is in " + rollout + "% rollout (bucket " + bucket + ")"
                : "User is outside " + rollout + "% rollout (bucket " + bucket + ")");

        return response;
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private FeatureFlag findByKeyOrThrow(String key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new FlagNotFoundException("Flag not found: " + key));
    }

    private FlagResponse toResponse(FeatureFlag flag) {
        FlagResponse r = new FlagResponse();
        r.setId(flag.getId());
        r.setKey(flag.getKey());
        r.setName(flag.getName());
        r.setDescription(flag.getDescription());
        r.setEnabled(flag.isEnabled());
        r.setRolloutPercentage(flag.getRolloutPercentage());
        r.setEnvironments(flag.getEnvironments());
        r.setAllowlist(flag.getAllowlist());
        r.setCreatedAt(flag.getCreatedAt() != null ? flag.getCreatedAt().format(FORMATTER) : null);
        r.setUpdatedAt(flag.getUpdatedAt() != null ? flag.getUpdatedAt().format(FORMATTER) : null);
        return r;
    }
}
