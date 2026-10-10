# Pariksharti · Java + React edition

UPSC preparation workspace with a **Java 17 / Spring Boot 3.5.16 REST backend**, **React 19 + Vite + TypeScript**, **Tailwind CSS and shadcn/ui components**. The blue, white, navy and grey design includes English/Hindi content.

Start with [the VS Code guide](docs/01-VSCODE-HINDI.md), then [GitHub](docs/02-GITHUB-HINDI.md), then [free deployment](docs/03-FREE-DEPLOYMENT-HINDI.md). Open this `java-react` folder itself in VS Code.

## Features

- Register, sign in, sign out and create/edit your learner profile.
- Account-specific syllabus progress, study schedules, daily checklist and answer drafts.
- 102 syllabus topics, books/resources, exam guide and preparation methods.
- Daily focus desk, 25/50-minute timer, 30-day beginner guide and six original answer exercises.
- BCrypt password hashes, HttpOnly session cookies, CSRF protection and server-side ownership checks.
- H2 file database locally; PostgreSQL with Flyway migrations for deployment.

## Local start

Install JDK 17 and Node.js 24 LTS. Maven downloads through the included wrapper.

Terminal 1, Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The local backend listens on port 8081 to avoid conflicts with Oracle's listener on port 8080.

Terminal 2:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Open http://localhost:5173. Create your own account; there is no default password. Linux/macOS: use `./mvnw` and `npm` instead of the Windows commands.

## Deployment shape

Docker builds React and puts its static files inside the Spring Boot jar. **One Render web service serves both frontend and API on the same HTTPS origin.** Neon PostgreSQL holds accounts and study data. No separate frontend host or CORS configuration is required for this setup.

Free hosting is suitable for learning and a small hobby app within provider limits; it is not an always-on production guarantee. See the deployment guide for current limits and costs.

## Tests

```sh
cd frontend
npm test
npm run build
cd ../backend
./mvnw test
```

When changing topic IDs/optional subjects, run `npm run export:catalog` from `frontend`, then commit the generated `backend/src/main/resources/catalog.json` too.

## Scope

No email verification, password-reset email, Google login, uploaded profile photos or administrator panel is included. Production registration requires a private invitation code. Login sessions are in server memory: restarting/sleeping the backend requires signing in again; PostgreSQL data remains saved. Existing Sites/Next.js data is not automatically imported into this new database.

See [architecture and API](docs/04-ARCHITECTURE.md). Deployment instructions were checked against official provider documentation on **8 October 2026**.
