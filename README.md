# URL Shortener

Small REST service for shortening links. You sign up, log in, create short links and can see
how many times each one was opened. Anyone who has a short link gets redirected to the original
URL, even without an account.

## Stack

Java 21, Spring Boot, Spring Data JPA, Spring Security with JWT, PostgreSQL, Flyway, springdoc
OpenAPI, JUnit 5 + Mockito + Testcontainers, Docker. CI runs on GitHub Actions.

## How to run

The easiest way is Docker Compose.

1. Copy the example env file and fill in the values:

```bash
cp .env.example .env
```

2. Start everything:

```bash
docker compose up --build
```

The app starts on http://localhost:8080 and Swagger UI is at
http://localhost:8080/swagger-ui.html.

If you prefer running `AppLauncher` from IntelliJ, start just the database with
`docker compose up -d db` and put the same env variables into the run configuration.

## Environment variables

Credentials and other secrets are not kept in the code, they are read from env variables.
See `.env.example` for a template.

- `DB_HOST` - database host (default `localhost`)
- `DB_PORT` - database port (default `5432`)
- `DB_NAME` - database name (default `url_shortener`)
- `DB_USERNAME` - database user (required)
- `DB_PASSWORD` - database password (required)
- `JWT_SECRET` - secret for signing tokens, at least 32 characters (required)
- `JWT_EXPIRATION_MS` - token lifetime in ms (default `86400000`, 24h)
- `SERVER_PORT` - app port (default `8080`)
- `APP_BASE_URL` - base URL used when building short links (default `http://localhost:8080`)
- `SHORT_LINK_TTL_DAYS` - default link lifetime in days (default `30`)

## Endpoints

Auth (no token needed):

- `POST /api/v1/auth/register` - register a new user
- `POST /api/v1/auth/login` - log in, returns a JWT

Links (need header `Authorization: Bearer <token>`):

- `POST /api/v1/links` - create a short link
- `GET /api/v1/links` - all my links with stats
- `GET /api/v1/links/active` - only active (not expired) links
- `GET /api/v1/links/{id}` - one link with stats
- `PUT /api/v1/links/{id}` - edit a link
- `DELETE /api/v1/links/{id}` - delete a link

Redirect (public):

- `GET /r/{shortCode}` - redirect to the original URL and count the visit

Password rule on registration: at least 8 characters, with a digit, a lowercase and an
uppercase letter.

## Tests

```bash
./gradlew test    # run tests
./gradlew build   # tests + 80% coverage check
```

Integration tests spin up a real PostgreSQL through Testcontainers, so Docker has to be running.
If there is no Docker, those tests are skipped and only the unit tests run. The coverage report
is in `build/reports/jacoco/test/html/index.html`.
