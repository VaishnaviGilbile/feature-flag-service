package com.example.featureflags.repository;

import com.example.featureflags.model.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    Optional<FeatureFlag> findByKey(String key);

    boolean existsByKey(String key);

    List<FeatureFlag> findAllByEnabled(boolean enabled);
}
