# Architecture and API

## Local and deployed app

Local: browser → Vite on 5173 → `/api` proxy → Spring Boot on 8081 → file-backed H2.

Deployed: browser → Render HTTPS → Spring Boot serves React assets and `/api` → Neon PostgreSQL.

React is plain Vite/TypeScript; this edition has no Next.js dependency. shadcn/ui component source is in `frontend/src/components/ui`, with Radix primitives. The auth/profile views use React state and a shadcn Sheet, all at the root URL.

## Backend code layout

Java sources live under `backend/src/main/java/in/pariksharti/desk`:

- `controller`: HTTP endpoints and request routing.
- `dto`: validated API requests and response records.
- `service`: account and planner business rules.
- `repository`: Spring Data JPA persistence interfaces.
- `model`: JPA entities.
- `config`: Spring Security and application configuration.
- `exception`: API exceptions and centralized error responses.

`DeskApplication` stays in the base package so Spring component and entity scanning includes all layers.

## Authentication

Register stores the account and an empty planner in a transaction. Passwords are BCrypt-hashed (cost 12); plaintext passwords are never returned. Email is normalized to lower case and unique in the database.

Login authenticates through Spring Security, rotates the session ID and CSRF token, then explicitly saves the security context. HttpOnly `DESKSESSION` cookie holds an opaque session ID. It uses SameSite=Lax and is Secure in production. The browser does not store an authentication token in localStorage. Only the sign-in page language preference is stored there.

The frontend gets a CSRF token from `/api/auth/csrf` and sends its header on state-changing requests, including register/login/logout. After login it discards the previous token and gets a fresh one. Server sessions are in memory and expire after two idle hours or a process restart. Data lives separately in the database.

Eight failed attempts for a given normalized email within fifteen minutes trigger a temporary limit. That limit is in memory and resets on restart. Signup is open by default, without an invitation code; turn off registrations through `APP_REGISTRATION_ENABLED=false` if only existing users should log in.

## API

| Method | Path | Access / purpose |
|---|---|---|
| GET | `/api/health` | Public process health |
| GET | `/api/auth/config` | Public registration/invitation-required flags |
| GET | `/api/auth/csrf` | Public CSRF token for this browser session |
| POST | `/api/auth/register` | CSRF; create account |
| POST | `/api/auth/login` | CSRF; authenticate and rotate session |
| POST | `/api/auth/logout` | CSRF; invalidate session; 204 response |
| GET | `/api/auth/me` | Current signed-in profile |
| PUT | `/api/profile` | Signed-in user's profile fields |
| GET | `/api/planner` | Signed-in user's planner |
| PATCH | `/api/planner` | Signed-in user's validated field update |

Register: `{fullName,email,password,inviteCode?}`. Profile: `{fullName,city,bio,targetYear}`. Email remains the sign-in identity and cannot be changed through the profile API in this version.

Planner PATCH: `{field,patch}`; fields are `progress`, `schedule`, `sessions`, `settings`. Topic IDs, statuses, real calendar dates, optional subjects, hour/month ranges, language and six practice-draft IDs are validated. Draft length is capped at 5,000 characters. A row lock serializes updates to a user's planner; partial draft updates merge by prompt ID. `null` removes a progress/schedule/checklist entry, while required settings cannot be deleted.

The authenticated UUID determines ownership. Endpoints do not accept a client-selected owner/user ID. Other users cannot request another account's planner or profile through these routes.

## Persistence and migrations

`accounts` holds profile fields and password hashes. `planners` holds the per-user validated JSON state with a foreign key to the account. Flyway applies schema migrations; Hibernate validates the result rather than changing schemas on startup. `V1` uses SQL compatible with H2 PostgreSQL mode and PostgreSQL. Add `V2`, `V3`, etc. for future schema changes.

Local data goes under `backend/data` (ignored by Git). Deployment uses only the PostgreSQL connection in environment variables. Hikari uses max four connections and min idle zero to allow an idle Neon database to sleep.

The checked-in `catalog.json` is exported from the frontend syllabus so both sides agree on IDs/optionals. CI exports it again and rejects a stale catalogue.

## Extending this project

Future features can include email verification/password reset, photo storage, Google login and an admin panel. They need separate implementations and provider configuration. Timer state runs in the browser; switching planner sections keeps it running, but closing/reloading the tab resets it. Answer drafts save on explicit Save, not on every keystroke. Revision suggestions use completed topics and are not a dated spaced-repetition system.

API/JUnit tests exercise the real Spring Security filter chain, CSRF/session rotation, validation, profile persistence and account isolation. Frontend tests cover syllabus/schedule logic and bilingual study content. Docker requires Docker Engine/Desktop to test locally; normal VS Code development does not.
