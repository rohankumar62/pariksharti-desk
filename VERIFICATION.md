# Verification — 8 October 2026

## Completed

- Clean npm dependency installation using the checked-in lockfile.
- Frontend TypeScript checking and Vite production build.
- Three frontend suites covering 102 unique topic IDs, combined filters, Hindi/English coverage, optional catalogue, real calendar boundaries, multi-day schedules, daily activity, practice-draft validation, word counts and timer logic.
- Eight Spring Boot/JUnit integration tests, with the real Spring Security filter chain and H2 PostgreSQL-mode migrations: registration, unique normalized email, BCrypt hashes, login failures/attempt limits, profile persistence, per-user planner isolation, partial draft merge, input validation, CSRF, session-ID/token rotation, logout and private invitation requirements.
- A packaged Spring Boot jar with the compiled React assets served at `/`.
- Real HTTP checks against that jar: static assets, register/login/profile/logout, HttpOnly/SameSite cookie, CSRF rejection, two-user separation, and saved profile/progress/Hindi answer draft after stopping and restarting the server.
- Source ZIP excludes installed dependencies, generated build output, databases and real credentials.

The environment used **OpenJDK 17.0.20**, **Maven 3.9.11** through the wrapper, **Node 24.19.0**, Spring Boot **3.5.16** and React **19.2.6**.

## Remaining checks when you deploy

- No external GitHub repo, Render service or Neon database was provisioned for this new edition. Test the actual PostgreSQL connection and HTTPS cookies after following the deployment guide.
- Docker Engine was unavailable here. The frontend/jar build and HTTP runtime were tested directly; the Docker image itself was not run.
- Browser visual/click testing was unavailable because the browser download failed. Check the login/profile sheet and desktop/mobile layout in your local browser.
- Maven/npm commands were run on Linux. Windows PowerShell commands are included, but were not executed on a Windows machine.

## Repeat checks

From `frontend`: `npm test` and `npm run build`.

From `backend`: Windows `.\mvnw.cmd test`; Linux/macOS `./mvnw test`.

Optional packaged HTTP check, from the `java-react` root on Linux/macOS:

```sh
mkdir -p backend/src/main/resources/static
cp -R frontend/dist/. backend/src/main/resources/static/
cd backend
./mvnw package
cd ..
node scripts/smoke-http.mjs
```

Windows PowerShell equivalent, after building the frontend:

```powershell
New-Item -ItemType Directory -Force backend/src/main/resources/static
Copy-Item frontend/dist/* backend/src/main/resources/static/ -Recurse -Force
cd backend
.\mvnw.cmd package
cd ..
node scripts/smoke-http.mjs
```

The HTTP script uses a temporary local database and deletes it on completion; it does not use your normal accounts or a cloud database.
