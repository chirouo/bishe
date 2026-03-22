# Repository Guidelines

## Project Structure & Module Organization
This repository now uses a simple front-end/back-end split suitable for a graduation project. Keep the root directory uncluttered and place work in these top-level directories:

- `backend/` for the Spring Boot application
- `frontend/` for the Vue application
- `docs/` for design notes, specs, and contributor-facing documentation
- `scripts/` for repeatable local automation such as database setup
- `tests/` for repository-level tests or future integration checks

Example:
`backend/src/main/java/...`, `frontend/src/views/...`, `docs/architecture.md`

## Build, Test, and Development Commands
Use the following stable command surface and keep this section updated when tooling changes:

- Initialize database:
  `mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql`
- Start backend dev server:
  `cd backend && mvn spring-boot:run`
- Build backend:
  `cd backend && mvn clean package`
- Start frontend dev server:
  `cd frontend && npm install && npm run dev`
- Build frontend:
  `cd frontend && npm run build`
- Run Python script checks in the dedicated Conda environment:
  `conda run -n qdx_bishe python <script.py>`

This project does not currently use `make`.

## Coding Style & Naming Conventions
Use the formatter and linter standard for the language you add, and commit only formatted code. Favor small modules, explicit names, and single-purpose files.

- Use UTF-8 text files and consistent indentation chosen by the stack
- Name directories and Markdown files in `kebab-case`
- Use `camelCase` in Vue/JavaScript where appropriate and `PascalCase` for Vue component filenames
- Use `PascalCase` for Java classes and `camelCase` for Java fields and methods
- Keep configuration files at the repository root only when they apply project-wide

## Testing Guidelines
Add automated tests with every non-trivial feature. Backend tests should usually live under `backend/src/test/`; front-end tests can be added under `frontend/src/__tests__/` if introduced later. Use `tests/` only for repository-level or cross-service checks.

- Prefer fast unit tests first
- Add at least one failure-path test for new logic
- For every small feature, add a few mock or demo records that exercise the feature and run the related tests before closing the task
- For every completed feature, perform browser-level verification with Playwright before considering the task done; API or unit tests can supplement this step but must not replace it
- Prefer reusing `tests/browser/full_browser_smoke.py`; if the feature needs a more focused path, add or extend a dedicated Playwright script and run it in the user-approved Conda environment
- Name tests after the behavior they verify, for example `tests/api/test_health.py` or `tests/auth/login.spec.ts`

## Commit & Pull Request Guidelines
There is no local Git history in this workspace yet, so no repository-specific commit pattern can be inferred. Start with concise, imperative commits, preferably Conventional Commit style, for example `feat: add initial project scaffold`.

Pull requests should stay focused and include:

- a short summary of the change
- linked issue or task reference, if any
- verification steps run locally
- screenshots or sample output when UI or generated artifacts change

## Maintenance Notes
If you introduce new structure, commands, or tooling, update this guide in the same change so future contributors are not forced to rediscover conventions.

- Keep the architecture simple unless requirements change: no Redis, no microservices, no unnecessary infrastructure.
- Treat "mock data + executed tests" as part of the definition of done for each incremental feature.
- Use the user-approved Conda environment `qdx_bishe` for future Python-based test scripts and related tooling in this repository unless there is a clear blocker.
- Treat "completed feature = browser-level verification executed" as a repository rule, not an optional extra step.
