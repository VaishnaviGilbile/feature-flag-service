# Feature Flag Service

A lightweight **feature flag management service** built with Spring Boot. Supports flag creation, rollout percentages, environment targeting, and per-user allowlists.

> Built as a portfolio/resume project demonstrating REST API design, Spring Boot, JPA, and feature flag concepts used in production systems like LaunchDarkly and Unleash.

---

## Features

- ✅ **CRUD** for feature flags via REST API
- 🎯 **Rollout percentage** — deterministic user bucketing (same user always gets the same result)
- 🌍 **Environment targeting** — `production`, `staging`, etc.
- 👤 **Allowlist** — specific users always get a flag regardless of rollout
- 🩺 **Health checks** via Spring Actuator
- 🗄️ **H2 in-memory DB** with console at `/h2-console`
- 🧪 **Integration tests** with MockMvc

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/feature-flag-service.git
cd feature-flag-service

# Run
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

Four sample flags are seeded automatically on startup.

---

## API Reference

### List all flags
```
GET /api/v1/flags
```

### Get a flag
```
GET /api/v1/flags/{key}
```

### Create a flag
```
POST /api/v1/flags
Content-Type: application/json

{
  "key": "new_feature",
  "name": "New Feature",
  "description": "Optional description",
  "enabled": true,
  "rolloutPercentage": 50,
  "environments": "production,staging",
  "allowlist": "user_001,user_002"
}
```

### Update a flag (partial)
```
PATCH /api/v1/flags/{key}
Content-Type: application/json

{
  "enabled": false,
  "rolloutPercentage": 25
}
```

### Delete a flag
```
DELETE /api/v1/flags/{key}
```

### Evaluate a flag for a user
```
POST /api/v1/flags/{key}/evaluate
Content-Type: application/json

{
  "userId": "user_123",
  "environment": "production"
}
```

**Response:**
```json
{
  "flagKey": "dark_mode",
  "userId": "user_123",
  "environment": "production",
  "enabled": true,
  "reason": "User is in 50% rollout (bucket 37)"
}
```

---

## Evaluation Logic

Flags are evaluated in this order:

| Step | Condition | Result |
|------|-----------|--------|
| 1 | Flag not found | `disabled` |
| 2 | Flag globally disabled | `disabled` |
| 3 | User in allowlist | `enabled` (bypass rollout) |
| 4 | Environment mismatch | `disabled` |
| 5 | Rollout % check (hash of flagKey + userId) | `enabled` or `disabled` |

The rollout bucketing is **deterministic** — the same user always gets the same result for the same flag.

---

## Monitoring

Spring Actuator endpoints:
- `GET /actuator/health` — health status
- `GET /actuator/info` — app info
- `GET /actuator/metrics` — JVM and HTTP metrics

---

## Running Tests

```bash
./mvnw test
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| API | Spring MVC (REST) |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, MockMvc |
| Boilerplate | Lombok |

---

## Project Structure

```
src/
├── main/java/com/example/featureflags/
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic & evaluation
│   ├── repository/       # Spring Data JPA
│   ├── model/            # JPA entity
│   ├── dto/              # Request/Response DTOs
│   └── exception/        # Custom exceptions + global handler
└── test/                 # Integration tests
```
