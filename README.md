# URL Shortener Service

A REST API for shortening long URLs. Registered users create short links, track click
statistics and manage their links; anyone can follow a short link and be redirected to the
original URL.

## Tech stack

- Java 21, Spring Boot 3.4
- Spring Web, Spring Data JPA, Spring Security (JWT)
- PostgreSQL + Flyway migrations
- OpenAPI 3.0 (springdoc / Swagger UI)
- JUnit 5, Mockito, Testcontainers
- JaCoCo (minimum 80% coverage, enforced on `build`)
- Docker, Docker Compose, GitHub Actions CI

## Project structure (package-as-feature)

```
org.example.urlshortener
├── AppLauncher            # application entry point (run this from IntelliJ IDEA)
├── auth                   # registration, login, JWT user details
├── user                   # user entity & repository
├── link                   # short link feature: create / list / update / delete / stats
├── redirect               # public redirect by short code
├── security               # JWT service, filter, security configuration
├── error                  # exceptions and global REST exception handler
└── config                 # OpenAPI and configuration properties
```

## Running the application

### Run from IntelliJ IDEA

Run the `AppLauncher` class (in the root package `org.example.urlshortener`). Before running,
provide the environment variables listed below (Run/Debug configuration → Environment variables),
and make sure a PostgreSQL database is reachable. The quickest way to get a database is:

```bash
docker compose up -d db
```

### Run everything with Docker Compose

```bash
cp .env.example .env   # then edit the values
docker compose up --build
```

The API will be available at `http://localhost:8080`.

## Environment variables

All sensitive configuration is supplied through environment variables. Copy `.env.example`
to `.env` and adjust the values.

| Variable              | Required | Default                 | Description                                                        |
|-----------------------|----------|-------------------------|--------------------------------------------------------------------|
| `DB_HOST`             | no       | `localhost`             | PostgreSQL host                                                    |
| `DB_PORT`             | no       | `5432`                  | PostgreSQL port                                                   |
| `DB_NAME`             | no       | `url_shortener`         | Database name                                                     |
| `DB_USERNAME`         | **yes**  | —                       | Database user                                                     |
| `DB_PASSWORD`         | **yes**  | —                       | Database password                                                 |
| `JWT_SECRET`          | **yes**  | —                       | HMAC secret for signing JWTs (must be at least 32 characters)     |
| `JWT_EXPIRATION_MS`   | no       | `86400000` (24h)        | Access token lifetime in milliseconds                            |
| `SERVER_PORT`         | no       | `8080`                  | HTTP port the application listens on                             |
| `APP_BASE_URL`        | no       | `http://localhost:8080` | Base URL used when building full short URLs in responses         |
| `SHORT_LINK_TTL_DAYS` | no       | `30`                    | Default expiration (in days) applied to a link when none is given |

> The database credentials and the JWT secret are **never** hard-coded — they are read from the
> environment as shown above.

## API documentation (Swagger / OpenAPI)

Once the application is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The API is versioned via the URI (`/api/v1/...`).

### Main endpoints

| Method   | Path                     | Auth | Description                                  |
|----------|--------------------------|------|----------------------------------------------|
| `POST`   | `/api/v1/auth/register`  | no   | Register a new user                          |
| `POST`   | `/api/v1/auth/login`     | no   | Authenticate and receive a JWT               |
| `POST`   | `/api/v1/links`          | yes  | Create a short link                          |
| `GET`    | `/api/v1/links`          | yes  | List all links of the current user + stats   |
| `GET`    | `/api/v1/links/active`   | yes  | List active (non-expired) links + stats      |
| `GET`    | `/api/v1/links/{id}`     | yes  | Get one link with statistics                 |
| `PUT`    | `/api/v1/links/{id}`     | yes  | Update an existing link                      |
| `DELETE` | `/api/v1/links/{id}`     | yes  | Delete a link                                |
| `GET`    | `/r/{shortCode}`         | no   | Redirect to the original URL (records visit) |

Authenticated endpoints require an `Authorization: Bearer <token>` header.

### Password policy

Passwords must be at least 8 characters long and contain digits, lowercase and uppercase
letters.

## Testing

```bash
./gradlew test          # run all tests
./gradlew build         # run tests + enforce 80% JaCoCo coverage
```

Integration tests start a real PostgreSQL instance using
[Testcontainers](https://java.testcontainers.org/), so **Docker must be running** to execute
them. When Docker is unavailable the integration tests are skipped automatically
(`@Testcontainers(disabledWithoutDocker = true)`), while the unit tests still run.

The JaCoCo HTML coverage report is generated at
`build/reports/jacoco/test/html/index.html`.

## Continuous integration

GitHub Actions (`.github/workflows/ci.yml`) builds the project, runs all tests (including the
Testcontainers-based integration tests, since the runner has Docker) and enforces the coverage
threshold on every push and pull request targeting `main`.
