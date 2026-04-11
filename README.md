# Mentor Service

AI-powered mentoring microservice for the **ForME** learning platform. Provides personalized learning advice, study plan generation, streak tracking, conversation threads, and learner analytics — all driven by Google Gemini AI.

---

## Overview

| Property | Value |
|---|---|
| Port | `8088` |
| Database | MySQL — `forme_mentor` |
| Spring Boot | 3.3.5 |
| Java | 17 |
| Service Discovery | Eureka (`http://localhost:8761/eureka/`) |

---

## Architecture

```
mentor-service
├── controller/       # REST endpoints (6 controllers)
├── service/          # Business logic (14 services)
├── entity/           # JPA entities (14 models)
├── repository/       # Spring Data JPA repositories
├── dto/              # Request/response DTOs
├── security/         # JWT filter, principal, token service
├── config/           # Security config, web client config
├── client/           # WebClient integrations (formation, cert, user services)
└── resources/
    └── db/migration/ # Flyway SQL migrations (V1–V10)
```

---

## Features

1. **AI Mentor Engine** — Google Gemini-powered answers, weekly briefs, exam tips, learning paths, remediation plans
2. **Learner Portfolio** — Skills, roles, learning style, extended preferences
3. **Study Plans** — AI-generated plans with trackable items, completion tracking, and AI motivational messages
4. **Persistent Conversations** — Multi-turn conversation threads with full history context
5. **Feedback System** — UP/DOWN ratings with comments on mentor advice
6. **Adaptive Difficulty Detection** — Detects repeated failures and declining exam scores
7. **Learning Streaks** — Gamified daily streak with email reminders (Gmail SMTP)
8. **Formation Demand Tracking** — Records skill-gap-driven requests for new formations
9. **In-App Notifications** — Admin broadcasts + targeted learner notifications
10. **Admin Analytics** — Usage stats, feedback dashboard, top streaks, demand analytics

---

## API Endpoints

### Learner Endpoints (`/mentor/**`) — requires authentication

#### Portfolio
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/portfolio/exists` | Check if portfolio exists |
| `GET` | `/mentor/portfolio` | Get portfolio with skills |
| `PUT` | `/mentor/portfolio` | Create or update portfolio |

#### AI Mentor
| Method | Path | Description |
|---|---|---|
| `POST` | `/mentor/ask` | Ask a question (rate-limited: 3000ms) |
| `GET` | `/mentor/weekly-brief` | Get weekly learning brief |
| `GET` | `/mentor/pre-exam-tips/{formationId}` | Pre-exam study tips |
| `GET` | `/mentor/learning-path` | Generate or refresh learning path |
| `GET` | `/mentor/advice/history` | Paginated history of AI advice (max 50/page) |

#### Study Plans
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/study-plans` | List all study plans |
| `POST` | `/mentor/remediation/{formationId}` | Generate remediation plan for a failed formation |
| `POST` | `/mentor/study-plans/items/{itemId}/complete` | Mark item complete (returns AI motivation message) |

#### Conversation Threads
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/threads` | List active threads |
| `POST` | `/mentor/threads` | Create a new thread |
| `GET` | `/mentor/threads/{threadId}` | Get thread with last 100 messages |
| `DELETE` | `/mentor/threads/{threadId}` | Archive a thread |

#### Feedback & Bookmarks
| Method | Path | Description |
|---|---|---|
| `POST` | `/mentor/feedback` | Submit UP/DOWN feedback on a session |
| `GET` | `/mentor/feedback/history` | All user feedback history |
| `POST` | `/mentor/advice/{sessionId}/bookmark` | Toggle bookmark (with optional note) |
| `GET` | `/mentor/advice/bookmarks` | Paginated bookmarked advice |

#### Misc
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/streak` | Get current and best streak |
| `GET` | `/mentor/difficulty-alerts` | Adaptive difficulty alerts (repeated failures, declining scores) |

#### Notifications (Learner)
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/me/notifications` | Get notifications targeted to the learner |
| `GET` | `/mentor/me/notifications/unread-count` | Count unread |
| `PATCH` | `/mentor/me/notifications/mark-all-read` | Mark all as read |
| `PATCH` | `/mentor/me/notifications/{id}/read` | Mark one as read |

---

### Admin Endpoints (`/mentor/admin/**`) — requires `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

#### User Management
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/admin/users` | List all users with portfolios |
| `GET` | `/mentor/admin/users/{userId}/summary` | Detailed user summary |

#### Statistics
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/admin/stats/usage` | Daily and monthly usage |
| `GET` | `/mentor/admin/stats/formation-demand` | Top 10 requested formations by role |
| `GET` | `/mentor/admin/stats/feedback` | UP/DOWN feedback counts |
| `GET` | `/mentor/admin/stats/top-streaks` | Top 10 learners by streak (with names) |

#### Feedback Dashboard
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/admin/feedback/dashboard` | Complete dashboard with optional date range |
| `GET` | `/mentor/admin/feedback/statistics` | Totals, satisfaction rate, sentiment |
| `GET` | `/mentor/admin/feedback/trends` | Daily trend data |
| `GET` | `/mentor/admin/feedback/comments` | Recent comments with ratings |

#### Formation Demands
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/admin/formation-demands` | Aggregated view grouped by role |
| `GET` | `/mentor/admin/formation-demands/by-role` | Demand count per role |
| `POST` | `/mentor/admin/formation-demands/{demandId}/fulfill` | Fulfill demand and notify learner |

#### Notifications (Admin)
| Method | Path | Description |
|---|---|---|
| `GET` | `/mentor/admin/notifications` | All admin broadcast notifications |
| `GET` | `/mentor/admin/notifications/unread-count` | Count unread |
| `PATCH` | `/mentor/admin/notifications/mark-all-read` | Mark all as read |
| `PATCH` | `/mentor/admin/notifications/{id}/read` | Mark one as read |

---

## Database Schema

Managed by **Flyway** (`ddl-auto: validate`). Migrations run automatically on startup.

| Migration | Description |
|---|---|
| `V1__init_mentor.sql` | Core tables: portfolio, skills, sessions, advice, study plans |
| `V2__rename_portfolio_current_role.sql` | Rename `current_role` → `present_role` (MySQL reserved word) |
| `V3__portfolio_extended_preferences.sql` | Add `extended_preferences` JSON column |
| `V4__feedback_threads.sql` | Add feedback, conversation threads, messages |
| `V5__bookmarked_advice.sql` | Add bookmarked advice table |
| `V6__streak_reminder.sql` | Add learner streak and reminder log tables |
| `V7__formation_demand.sql` | Add formation demand tracking table |
| `V8__study_plan_item_completion.sql` | Add `completed` / `completed_at` to study plan items |
| `V9__mentor_notifications.sql` | Add mentor notification table and `fulfilled` flag on demands |
| `V10__fix_notification_type_column.sql` | Convert `type` column to `VARCHAR(40)` for Hibernate compatibility |

### Entity Summary

| Entity | Table | Key Relationships |
|---|---|---|
| `LearnerPortfolio` | `learner_portfolio` | One-to-many → `LearnerSkill` |
| `LearnerSkill` | `learner_skill` | Many-to-one → `LearnerPortfolio` |
| `MentorSession` | `mentor_session` | One-to-many → `MentorAdvice` |
| `MentorAdvice` | `mentor_advice` | Many-to-one → `MentorSession` |
| `StudyPlan` | `study_plan` | One-to-many → `StudyPlanItem` |
| `StudyPlanItem` | `study_plan_item` | Many-to-one → `StudyPlan` |
| `MentorFeedback` | `mentor_feedback` | Unique per session + user |
| `ConversationThread` | `conversation_thread` | One-to-many → `ConversationMessage` |
| `ConversationMessage` | `conversation_message` | Many-to-one → `ConversationThread` |
| `BookmarkedAdvice` | `bookmarked_advice` | Unique per user + session |
| `LearnerStreak` | `learner_streak` | One per user |
| `ReminderLog` | `reminder_log` | Deduplicated by user + date + type |
| `FormationDemand` | `formation_demand` | Tracks unfulfilled formation requests |
| `MentorNotification` | `mentor_notification` | `target_user_id = NULL` = admin broadcast |

---

## Security

- **Mechanism:** Stateless JWT via `Authorization: Bearer <token>` header
- **JWT Claims:** `uid` (user ID), `sub` (email), `roles` (list)
- **Roles:** `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Protected routes:**
  - `/mentor/admin/**` — requires ADMIN or SUPER_ADMIN
  - `/mentor/**` — requires any authenticated user
  - All other paths — denied by default
- **CSRF:** Disabled (stateless API)

---

## External Integrations

### Google Gemini AI
- **Primary model:** `gemini-2.5-flash`
- **Fallback models:** `gemini-2.0-flash` → `gemini-1.5-flash` → `gemini-1.5-pro`
- **Config key:** `google.ai.api-key`
- **Timeout:** 60 seconds
- Used for: mentor answers, study plans, weekly briefs, exam tips, remediation, motivation messages

### Gmail SMTP (Email Reminders)
- Daily streak reminder cron job (default: `0 0 8 * * *` — 8 AM)
- Two reminder types: `STREAK_AT_RISK` and `STREAK_BROKEN`
- Deduplication via `reminder_log` table (one email per user per day per type)

### Microservice Clients (WebClient via Eureka)

| Client | Target Service | Purpose |
|---|---|---|
| `FormationProgressClient` | `formation-service` | Exam attempts, scores, completion status |
| `FormationCatalogClient` | `formation-service` | Published formation list for AI recommendations |
| `CertificationMentorClient` | `certification-service` | Learner certification context |
| `UserProfileClient` | `user-service` | User email + name (for reminders and analytics) |

All clients use **Spring Cloud LoadBalancer** for service discovery via Eureka.

---

## Configuration

Key environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/forme_mentor` | Database connection URL |
| `DB_PASSWORD` | *(empty)* | MySQL password |
| `APP_JWT_SECRET` | `ThisIsA32+CharSecretKeyChangeMeNow!!!` | JWT signing secret — **change in production** |
| `GOOGLE_AI_API_KEY` | *(hardcoded fallback)* | Gemini AI API key |
| `MAIL_USERNAME` | *(empty)* | Gmail address for sending emails |
| `MAIL_PASSWORD` | *(empty)* | Gmail app password |
| `MENTOR_REMINDER_ENABLED` | `true` | Enable/disable streak reminder scheduler |
| `MENTOR_REMINDER_CRON` | `0 0 8 * * *` | Cron expression for daily reminders |
| `FORMATION_INTERNAL_TOKEN` | *(empty)* | Internal token for cross-service calls |

---

## Running Locally

**Prerequisites:** Java 17, MySQL with `forme_mentor` database created, Eureka server running on port 8761.

```bash
# From the mentor-service directory
./mvnw spring-boot:run
```

Flyway migrations run automatically on startup. The service registers with Eureka at `http://localhost:8761/eureka/`.

**Via Docker Compose** (from the project root):
```bash
docker-compose up mentor-service
```

---

## API Gateway Routing

Through the API Gateway (port 8082), mentor-service endpoints are accessible under `/api/mentor/**`:

```
GET  http://localhost:8082/api/mentor/portfolio
POST http://localhost:8082/api/mentor/ask
GET  http://localhost:8082/api/mentor/admin/stats/usage
```

The gateway strips the `/api` prefix before forwarding to the service.

---

## Notes

- The `ask` endpoint enforces a **3-second rate limit** per user to prevent AI API abuse.
- Study plan completion triggers an **AI-generated motivational message** referencing the completed item and the next task. A special celebration message is returned when the entire plan is finished.
- Formation demands are automatically detected when the AI identifies a skills gap matching available formations in the catalog.
- Admin notifications use `target_user_id = NULL` as a broadcast mechanism — all admins see them.
