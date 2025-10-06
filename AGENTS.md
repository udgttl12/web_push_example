# Repository Guidelines

## Project Structure & Module Organization
Spring Boot sources live in `src/main/java/com/example/webpush`, split by layer: `config` for framework wiring, `controller` for REST entry points, `dto` for payload models, `entity` for JPA records, `repository` for data access, and `service` for business logic. Static client files (`index.html`, `app.js`, `sw.js`) reside in `src/main/resources/static`; database DDL is stored at `src/main/resources/db/schema.sql`. Default configuration lives in `src/main/resources/application.yml` and should be overridden with environment variables in non-local environments.

## Build, Test, and Development Commands
Use the Gradle wrapper at the project root. `./gradlew bootRun` (or `gradlew.bat bootRun`) starts the API plus static assets on port 8080. `./gradlew build` emits `build/libs/web-push-mvp-0.0.1-SNAPSHOT.jar`; invoke it with `java -jar` for packaged runs. `./gradlew test` executes the JUnit 5 suite. Prepare local MariaDB with `mysql -u root -p < src/main/resources/db/schema.sql` before exercising subscription endpoints.

## Coding Style & Naming Conventions
Code targets Java 17 and Spring Boot 3.4.1 with Lombok for constructors (`@RequiredArgsConstructor` plus `final` fields). Use four-space indentation, brace-on-same-line formatting, and meaningful method/variable names in lowerCamelCase. Keep DTOs suffixed with `Request`/`Response`, services with `Service`, repositories with `Repository`, and expose new REST handlers via controller classes. Favor constructor injection and avoid field-level `@Autowired`.

## Testing Guidelines
Tests belong in `src/test/java/com/example/webpush`, mirroring the main package layout. Name classes with the `*Test` suffix (e.g., `PushSubscriptionServiceTest`) and structure them around the behavior under test. Leverage `@SpringBootTest` for full flow coverage and lighter `@DataJpaTest` or Mockito-based slices for repositories and services. When verifying push delivery, mock external web-push clients and assert database side-effects. Run `./gradlew test` (or the IDE equivalent) before opening a PR.

## Commit & Pull Request Guidelines
History favors concise, sentence-case summaries (e.g., `Spring Boot web push MVP build`); keep commit titles under 72 characters and focus each change on a single concern. Add an optional body for schema updates, VAPID handling, or front-end changes that need extra context. PRs should include a short description, list notable modules touched, call out config or SQL changes, link tracking issues, and attach screenshots when UI assets change. Document the commands or tests you ran so reviewers can reproduce them quickly.
