package com.example.featureflags;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FeatureFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndGetFlag() throws Exception {
        String payload = """
            {
              "key": "test_flag",
              "name": "Test Flag",
              "description": "A test flag",
              "enabled": true,
              "rolloutPercentage": 100
            }
            """;

        mockMvc.perform(post("/api/v1/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("test_flag"))
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(get("/api/v1/flags/test_flag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Flag"));
    }

    @Test
    void shouldReturn404ForUnknownFlag() throws Exception {
        mockMvc.perform(get("/api/v1/flags/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldEvaluateFlagForUser() throws Exception {
        // First create a flag
        String create = """
            {"key": "eval_flag", "name": "Eval Flag", "enabled": true, "rolloutPercentage": 100}
            """;
        mockMvc.perform(post("/api/v1/flags")
                .contentType(MediaType.APPLICATION_JSON).content(create));

        // Then evaluate it
        String eval = """
            {"userId": "user_123", "environment": "production"}
            """;
        mockMvc.perform(post("/api/v1/flags/eval_flag/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eval))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.userId").value("user_123"));
    }

    @Test
    void shouldReturn409ForDuplicateKey() throws Exception {
        String payload = """
            {"key": "dup_flag", "name": "Dup", "enabled": false, "rolloutPercentage": 50}
            """;
        mockMvc.perform(post("/api/v1/flags")
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/flags")
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }
}
