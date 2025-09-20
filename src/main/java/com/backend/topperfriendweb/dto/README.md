# DTO Conventions and Tech Stack

This document describes how Data Transfer Objects (DTOs) are organized and named in this codebase, and a quick overview of the tech used.

## DTO Conventions

- **Purpose**
  - DTOs define the exact input and output contracts of API endpoints. They decouple controllers/services from persistence entities.

- **Naming**
  - **Request payloads** end with `Request` (e.g., `CreateNoteRequest`, `SubmitQuizRequest`, `UpdateStudyPlanTitleRequest`).
  - **Response models / resource views** end with `DTO` (e.g., `NoteDTO`, `QuizDTO`, `StudyPlanDTO`, `UserProfileDTO`).
  - **Auth flows** may additionally use `Response` (e.g., `LoginResponse`, `VerifyOtpResponse`) when the payload is not a full resource.

- **Packages**
  - Grouped by domain:
    - `dto/auth/` — registration, login, onboarding, OTP, Google callbacks
    - `dto/note/` — notes (create, list, pagination)
    - `dto/collection/` — collections and items
    - `dto/quiz/` — quiz generation, submission
    - `dto/studyplan/` — study plan operations
    - `dto/userprofile/` — profile views

- **Validation**
  - Use Jakarta Bean Validation for inputs: `@NotBlank`, `@NotNull`, `@Email`, `@Min`, `@Max`, etc.
  - Controllers annotate request bodies with `@Valid`. A global exception handler standardizes error responses.

- **Serialization Stability**
  - DTO field names are stable to avoid breaking the frontend. Any internal refactor SHOULD NOT change DTO JSON unless explicitly intended.

- **Mapping**
  - Mappers live under `mapper/` (e.g., `NoteMapper`, `UserMapper`). Services/controllers should not manually assemble DTOs.

## Service Layer Conventions

- **Facade + Focused Services**
  - Each domain exposes a thin `*Service` facade that keeps the public API stable.
  - Internals are split by responsibility, e.g., `NoteQueryService` (reads) and `NoteMutationService` (writes).

- **Transactions**
  - Mutations use `@Transactional`; queries use `@Transactional(readOnly = true)`.

## Error Handling

- A `GlobalExceptionHandler` (`exception/GlobalExceptionHandler.java`) converts exceptions into consistent `CommonResponse` payloads with proper HTTP status codes.

## Tech Stack (Quick Overview)

- **Spring Boot** — web application framework
- **Spring Web / MVC** — REST controllers
- **Spring Data JPA** — repositories for persistence
- **Spring Validation (Jakarta)** — `@Valid`, `@NotBlank`, etc.
- **Spring Security** — authentication/authorization; JWT handling via a `JwtUtil`
- **Google OAuth** — `GoogleAuthService` handles token exchange and profile fetch
- **Mail** — `EmailService` wraps `MailjetService` for OTP emails
- **AI (Gemini)** — `GeminiService` integrates with Google Generative Language API
- **Lombok** — boilerplate reduction (`@Data`, `@RequiredArgsConstructor`, etc.)
- **DTO Mappers** — centralized under `mapper/`

## Guidelines for Adding New DTOs

1. Place under the correct domain package in `dto/`.
2. Name it with `Request` for inputs, `DTO` for outputs.
3. Add appropriate validation annotations.
4. Update or add a mapper method when mapping from entities.
5. Keep JSON field names backward compatible.

## Notes

- Keep controllers light; push business logic into services.
- Prefer mappers for conversions; avoid manual mapping in services/controllers.
- Keep DTOs free of domain logic.
