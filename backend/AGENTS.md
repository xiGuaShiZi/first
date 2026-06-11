# AGENTS — How to work with this codebase (for AI coding agents)

Checklist (what I'll assume you need immediately):
- Understand the high-level architecture and where to change API behavior
- Know how to run/build/debug locally and what environment is required
- Know authentication, uploads, caching, and database integration points
- Follow project-specific conventions (status normalization, image path rules, token behavior)

Quick big-picture
- This is a Spring Boot Java 17 monolith (entry: `com.example.enterprise.EnterpriseWebsiteApplication`).
- Package layout (important):
  - `controller/` — HTTP API endpoints (routes grouped by purpose: `AuthController`, `PublicController`, `AdminController`, `UserController`, `MerchantController`, `UploadController`).
  - `service/` — business logic (e.g. `AuthService`, `ContentService`).
  - `repository/` — Spring Data JPA repositories (MariaDB assumed runtime).
  - `entity/`, `dto/`, `dto/vo` — domain models and request/response DTOs.
  - `config/` — app wiring: `SecurityConfig`, `WebConfig` (static resources + interceptors), `RedisConfig`, `DataInitializer`.

Core runtime expectations
- Build: Maven (see `pom.xml`) — Java 17. Common commands:
  - Build package: `mvn package` (or `mvn -DskipTests package`)
  - Run from maven: `mvn spring-boot:run`
  - Run the fat jar: `java -jar target\enterprise-website-1.0.0.jar`
- Environment variables / application properties (defaults in `target/classes/application.yml`):
  - DB: `DB_URL` (default jdbc:mariadb://127.0.0.1:3306/enterprise_website), `DB_USERNAME`, `DB_PASSWORD`
  - JPA DDL: `JPA_DDL_AUTO` (default `update`)
  - Upload: `UPLOAD_DIR` (default `uploads`), `PUBLIC_UPLOAD_PREFIX` (default `/uploads/`)
  - Admin initial password: `ADMIN_INITIAL_PASSWORD` (default `123456`)
  - CORS: `CORS_ALLOWED_ORIGINS` (defaults include http://localhost:5173)
  - Auth: `TOKEN_TTL_MINUTES`, `CAPTCHA_TTL_MINUTES`
  - Redis host/port: defaults `localhost:6379` but Redis is optional

Authentication and permissions (critical)
- Token-based auth is custom, not JWT. See `AuthService` and `SecurityConfig`:
  - `AuthService` stores tokens in an in-memory Map (`tokenStore`) with expiry. Tokens are opaque strings returned by login endpoints.
  - Tokens must be supplied as an HTTP header: `Authorization: Bearer <token>`.
  - Because tokens are in-memory, tokens are NOT shared across multiple application instances — note for load-balanced environments.
  - Admin/area mapping enforced by `SecurityConfig.TokenFilter`: routes starting with `/api/admin/` require `ADMIN` role, `/api/user/` require `USER`, `/api/merchant/` require `MERCHANT`.
  - Admin permission mapping rules and codes are defined in `SecurityConfig.requiredAdminPermission(...)` and seeded in `config/DataInitializer` (e.g. `dashboard:view`, `company:update`, `product:update`, `rbac:update`).
  - Default admin account is created/updated at startup by `DataInitializer` with username `admin` and password from `app.initial-admin-password`.

Important API patterns & conventions
- API route prefixes:
  - `/api/auth/**` — authentication & captcha (`AuthController`)
  - `/api/public/**` — unauthenticated public APIs (`PublicController`)
  - `/api/admin/**`, `/api/user/**`, `/api/merchant/**` — protected areas (see SecurityConfig)
- Controllers return a common wrapper `com.example.enterprise.common.Result<T>` — preserve this when adding new endpoints.
- Content and HTML fields are sanitized using Jsoup in `ContentService.sanitizeContentHtml(...)`; follow the same sanitization when adding rich-text fields.
- Status normalization: integer status fields are normalized to 0 or 1 using `normalizeStatus(...)` patterns (throw on invalid values). Follow this convention for any entity status handling.
- Image/file storage:
  - Uploads go to local filesystem (`app.upload-dir`, default `uploads`). See `UploadController` for validation rules (content-type → suffix mapping, size limits). Returned file URL is `app.public-url-prefix + filename`.
  - Static mapping `/uploads/**` → local upload dir is configured in `WebConfig.addResourceHandlers(...)`.
  - ContentService enforces server-uploaded image paths only: `validateServerImagePath(...)` rejects `http://`, `https://` and `data:` URIs and requires the configured `PUBLIC_UPLOAD_PREFIX`.

Caching and Redis
- `StringRedisTemplate` is injected into `ContentService` and used for cache DELETE operations. However, read/write cache behavior is disabled intentionally (`writeCache/readCache` are no-ops); deleteCache is wrapped and tolerated to allow optional Redis.
- Treat Redis as optional: the app must function without Redis (Redis exceptions are silently ignored around cache delete).

Data and DB considerations
- Uses MariaDB driver (runtime). Entities are mapped via Spring Data JPA. Verify DB schema exists or allow `spring.jpa.hibernate.ddl-auto` to manage it (defaults in `application.yml`).
- `DataInitializer` seeds permissions, roles, and admin user on startup — useful when testing RBAC flows.

Developer workflows / debug tips
- To run locally from IDE: run `EnterpriseWebsiteApplication.main()` (Spring Boot).
- To debug authentication flows, set breakpoints in `AuthService` (login/unifiedLogin/currentOrNull) and in `SecurityConfig.TokenFilter`.
- Logs: when running from the jar, check `target/*.log` files if present. The project currently writes build artifacts to `target/` including `enterprise-website-1.0.0.jar`.
- If you need persistent tokens or clustering, move token storage to Redis (AuthService currently uses in-memory `tokenStore`). `RedisConfig` provides a `RedisTemplate` bean you can reuse.

Files you should open first when changing behavior
- `src/main/java/com/example/enterprise/config/SecurityConfig.java` — auth filter and URL permission mapping
- `src/main/java/com/example/enterprise/service/AuthService.java` — token lifecycle and login flows (in-memory tokens)
- `src/main/java/com/example/enterprise/service/ContentService.java` — CMS logic, cache hooks, sanitization, image path checks
- `src/main/java/com/example/enterprise/controller/UploadController.java` — upload validation rules and URL generation
- `src/main/java/com/example/enterprise/config/DataInitializer.java` — seeded roles/permissions and admin account
- `pom.xml` and `target/classes/application.yml` — build/runtime settings and environment variable names

Small gotchas / conventions to preserve
- Do not return raw passwords — controllers null out the password before returning entities (see `AuthController.register` / `registerMerchant`).
- File validation is strict: prefer `UploadController.store(...)` logic when adding new upload endpoints.
- Cache writes are intentionally disabled to avoid stale external data; if you enable them, follow existing deleteCache safety checks.
- If adding new admin APIs under `/api/admin/**`, update `SecurityConfig.requiredAdminPermission(...)` if they should be permission-protected or ensure the controller method is allowed.

Example snippets (where to find important code):
- Admin permission seeds: `src/main/java/com/example/enterprise/config/DataInitializer.java` lines ~90–103
- Token check and role mapping: `src/main/java/com/example/enterprise/config/SecurityConfig.java` (`TokenFilter` class)
- Image path validation: `src/main/java/com/example/enterprise/service/ContentService.java` (`validateServerImagePath`)
- Upload constraints (5MB / 50MB): `src/main/java/com/example/enterprise/controller/UploadController.java`

If you need me to also:
- Add a short README explaining how to run with a local MariaDB container
- Convert in-memory tokenStore to Redis-backed sessions for clustering
- Add Postman/HTTP examples for each auth flow

-- End of AGENTS guidance

