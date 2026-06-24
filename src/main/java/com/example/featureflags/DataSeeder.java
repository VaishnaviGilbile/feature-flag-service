package com.example.featureflags;

import com.example.featureflags.model.FeatureFlag;
import com.example.featureflags.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final FeatureFlagRepository repository;

    @Override
    public void run(String... args) {
        // Only seed if table is empty — safe for both H2 and PostgreSQL
        if (repository.count() == 0) {
            repository.save(flag("new_dashboard", "New Dashboard UI",
                    "Redesigned dashboard for all users", true, 100, null, null));

            repository.save(flag("dark_mode", "Dark Mode",
                    "Dark mode toggle in settings", true, 50, "production,staging", null));

            repository.save(flag("beta_search", "Beta Search",
                    "Improved search with ML ranking", false, 20, "staging", "user_001,user_002"));

            repository.save(flag("checkout_v2", "Checkout V2",
                    "Revamped checkout flow", true, 10, "production", null));

            System.out.println("✅ Seeded 4 sample feature flags");
        }
    }

    private FeatureFlag flag(String key, String name, String description,
                             boolean enabled, int rollout, String envs, String allowlist) {
        FeatureFlag f = new FeatureFlag();
        f.setKey(key);
        f.setName(name);
        f.setDescription(description);
        f.setEnabled(enabled);
        f.setRolloutPercentage(rollout);
        f.setEnvironments(envs);
        f.setAllowlist(allowlist);
        return f;
    }
}
