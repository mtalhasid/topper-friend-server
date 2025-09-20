🎯 Code Quality# TopperFriend Web – Backend

A Spring Boot backend powering TopperFriend: authentication (email OTP + Google OAuth), notes, collections, quiz generation and study plans with Gemini integration. Production-friendly structure with clean service boundaries, centralized mappers, validation, and global error handling.

## Table of Contents
- Tech Stack
- Setup / Installation
- Environment Variables
- Database Setup
- Authentication
- Project Structure
- API Docs (Swagger)

## Tech Stack
- Spring Boot 3 (Web MVC, Validation, Security)
- Spring Data JPA with Hibernate ORM (PostgreSQL driver)
- Hibernate (JPA provider) for ORM and DDL auto (dev)
- JWT (jjwt) for stateless auth
- Spring Security (authorization, pre/post annotations)
- Email via Mailjet (wrapped by `EmailService`)
- Google OAuth (token exchange + user info)
- HTTP clients:
  - WebClient (Gemini API)
  - RestTemplate (Google OAuth userinfo)
- Resilience4j Circuit Breaker (Gemini integration)
- Lombok (boilerplate reduction)
- Springdoc OpenAPI (Swagger UI)
- DevTools (hot reload in dev)

## Setup / Installation
1. Prerequisites: Java 17, Maven, PostgreSQL
2. Configure `src/main/resources/application.properties` (see Environment Variables).
3. Build and run:
   - Build: `mvn -q -DskipTests=true clean package`
   - Run: `mvn spring-boot:run`
4. Swagger UI: open http://localhost:8080/swagger-ui.html

## Environment Variables
Configure in `src/main/resources/application.properties` (use your own secure values):
- Database
  - `spring.datasource.url` – JDBC URL
  - `spring.datasource.username` – DB username
  - `spring.datasource.password` – DB password
- JWT
  - `app.jwt.secret` – HMAC secret for JWT signing
  - `app.jwt.expiration-ms` – Token expiration ms
  - `app.jwt.clock-skew-seconds` – Parser clock skew seconds
- Mail / Email Verification
  - `mailjet.apiKey`, `mailjet.apiSecret`, `mailjet.fromEmail`, `mailjet.fromName`
- Gemini
  - `gemini.apiKey` – Google Generative Language API key
- Google OAuth
  - `google.oauth.client-id`, `google.oauth.client-secret`
  - `google.oauth.redirect-uri` – frontend callback URL
- CORS
  - `app.cors.allowed-origins` – comma-separated origins (e.g., `http://localhost:3000`)
- Resilience4j (optional defaults present)
  - `resilience4j.circuitbreaker.instances.gemini.*`

## Database Setup
- Create a PostgreSQL database and user.
- Set JDBC URL/credentials in properties.
- JPA is configured with `spring.jpa.hibernate.ddl-auto=update` (adjust per environment).
- For production, consider using migrations (Flyway/Liquibase).

## Authentication
- Email + Password
  - Registration stores a temp record, sends OTP via Mailjet.
  - OTP verify moves user into main users table.
- JWT
  - Generated on login/verify. Includes `userId`, `email`, `name`.
- Google OAuth
  - `GoogleAuthService` exchanges code for tokens, fetches profile.
  - Callback handled by `AuthService.handleGoogleCallback(...)`.
  - Optional OAuth `state` validation via `OAuthStateService`.

## Project Structure
```
src/main/java/com/backend/topperfriendweb/
  controller/           # Thin REST controllers
  service/
    auth/               # AuthService, GoogleAuthService, OtpService, EmailService, OAuthStateService
    note/               # NoteService facade + NoteQueryService/NoteMutationService
    quiz/               # QuizService facade + QuizQueryService/QuizMutationService + GeminiService
    studyplan/          # StudyPlanService facade + StudyPlanQueryService/StudyPlanMutationService + AiChatService
    collection/         # CollectionService
  repository/           # Spring Data JPA repositories
  mapper/               # DTO mappers (NoteMapper, UserMapper)
  dto/                  # Request/Response DTOs by domain (see dto/README.md)
  exception/            # GlobalExceptionHandler
  config/               # WebConfig (CORS)
  utils/                # JwtUtil
resources/
  application.properties
```

## API Docs (Swagger)
- After running the app, open Swagger UI:
  - http://localhost:8080/swagger-ui.html
  - OpenAPI JSON: http://localhost:8080/v3/api-docs

---

Tip: For quick manual testing, use `test.http` in the project root. It contains sample requests for the major endpoints.