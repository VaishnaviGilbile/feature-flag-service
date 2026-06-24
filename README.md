# Feature Flag Service

A feature flag management service built with Spring Boot. Toggle features on/off in production without redeploying — supports gradual rollouts, kill switches, environment targeting, and user allowlists.

**[Live Demo](https://your-app.onrender.com)**

---

## Tech Stack
Java 17 · Spring Boot 3.2 · PostgreSQL · Spring Data JPA · Docker · Render

---

## Run Locally
```bash
git clone https://github.com/YOUR_USERNAME/feature-flag-service.git
cd feature-flag-service
./mvnw spring-boot:run
```
Open `http://localhost:8080`

---

## API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/flags` | List all flags |
| POST | `/api/v1/flags` | Create a flag |
| PATCH | `/api/v1/flags/{key}` | Update a flag |
| DELETE | `/api/v1/flags/{key}` | Delete a flag |
| POST | `/api/v1/flags/{key}/evaluate` | Evaluate for a user |

**Evaluate example:**
```bash
curl -X POST http://localhost:8080/api/v1/flags/dark_mode/evaluate \
  -H "Content-Type: application/json" \
  -d '{ "userId": "user_123", "environment": "production" }'
```
```json
{
  "flagKey": "dark_mode",
  "userId": "user_123",
  "enabled": true,
  "reason": "User is in 50% rollout (bucket 37)"
}
```

---

## Health Check
```
GET /actuator/health
```
