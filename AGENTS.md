# Repository Guidelines

## Project Structure & Module Organization
This repository is split into three main areas:
- `backend/`: Spring Boot 4 + JPA service (Java 25, Gradle). Source is in `backend/src/main/java`, config in `backend/src/main/resources`, and tests should live in `backend/src/test/java`.
- `frontend/`: React + TypeScript + Vite app. App code is in `frontend/src` (feature code under `src/features`, shared UI in `src/components`, API client/types in `src/api`).

## Build, Test, and Development Commands
Backend (from `backend/`):
- `./gradlew bootRun`: run the API locally.
- `./gradlew test`: run JUnit tests.
- `./gradlew build`: compile, test, and package.
- `./gradlew bootBuildImage`: build container image (used in CI).

Frontend (from `frontend/`):
- `bun install --frozen-lockfile` (or `npm install`): install deps.
- `bun run dev` (or `npm run dev`): start Vite dev server.
- `bun run build`: type-check and build production assets.
- `bun run lint`: run ESLint.

## Coding Style & Naming Conventions
- Java: follow existing Spring conventions, `PascalCase` classes, `camelCase` fields/methods, package-by-feature under `com.jamesdpeters.SequenceGame...`.
- TypeScript/React: functional components, `PascalCase` component files, `camelCase` utilities/hooks.
- Match existing file style per module: backend files commonly use tabs; frontend uses ESLint defaults and semicolon-based style.

## Testing Guidelines
- Backend uses JUnit Platform via Gradle (`./gradlew test`) with `application-test.properties` and H2.
- Add tests under `backend/src/test/java` with names ending in `*Test`.
- Frontend currently has lint/type/build checks but no established test runner; add one in PRs that introduce complex UI logic.

## Commit & Pull Request Guidelines
- Keep commit messages short, imperative, and specific (examples in history: `Fix index error on first startup`, `Fix Dockerfile`).
- Reference issue/PR numbers when relevant (e.g., `... (#9)`).
- PRs should include:
  1. What changed and why.
  2. Impacted modules (`backend`, `frontend`, or both).
  3. Verification steps run locally (`./gradlew test`, `bun run lint`, etc.).
  4. Screenshots/video for UI changes.

## Security & Configuration Tips
- Do not commit secrets. Use environment variables for tokens and deploy credentials.
- Keep local overrides in profile-specific properties files and CI secrets.
