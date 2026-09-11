# AGENTS.md

## Project Name

SmartTask AI

## Purpose

SmartTask AI is a beginner-friendly Spring Boot portfolio project.

It is a task management application that demonstrates:

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- HTML
- CSS
- Bootstrap
- MySQL
- Spring Security
- AI integration
- Simple business automation

The application must remain easy for a fresher to understand and explain.

---

## Technology

Use:

- Java 17
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Spring Data JPA
- Thymeleaf
- HTML
- CSS
- Bootstrap
- Spring Validation
- Spring Security
- H2 for early development
- MySQL for the final database
- Spring AI for AI integration

Use only one AI provider.

Preferred:

- Google Gemini

Alternative:

- OpenAI

Do not integrate multiple AI providers unless explicitly requested.

---

## Architecture

Use a simple layered architecture:

```text
Controller
   ->
Service
   ->
Repository
   ->
Database
```

AI calls must follow:

```text
Controller
   ->
TaskService
   ->
AiService
   ->
AI Provider
```

Controllers must not call repositories directly.

Controllers must not call AI APIs directly.

---

## Package Structure

Prefer:

```text
com.smarttask

controller
service
repository
entity
dto
config
security
```

Do not create extra packages without a clear reason.

---

## Core Domain

The main entity is `Task`.

Expected fields:

```text
id
title
description
category
priority
status
aiSummary
createdAt
```

Suggested status values:

```text
OPEN
NEEDS_ATTENTION
COMPLETED
```

Suggested priority values:

```text
LOW
MEDIUM
HIGH
```

---

## AI Responsibility

AI must only analyze tasks.

When a task is created, AI should return:

```text
category
priority
summary
```

Represent this result with:

```java
public record TaskAiResult(
        String category,
        String priority,
        String summary
) {
}
```

Do not use AI for unrelated features unless requested.

---

## Automation Rule

Keep automation simple.

Required rule:

```text
If priority is HIGH:
    status = NEEDS_ATTENTION

Otherwise:
    status = OPEN
```

This rule belongs in the service layer.

Do not introduce a workflow engine.

---

## Coding Rules

Prefer readable beginner-friendly code.

Use:

- constructor injection
- clear method names
- standard Spring annotations
- small focused classes
- simple DTOs
- Jakarta Validation for form validation

Avoid:

- field injection
- unnecessary interfaces
- excessive abstraction
- clever code
- advanced functional programming
- unnecessary generics
- deeply nested inheritance
- complicated design patterns
- premature optimization

Do not use Lombok unless explicitly requested.

The code should be easy to explain in an interview.

---

## Spring MVC Rules

Use Thymeleaf for the UI.

Expected templates:

```text
index.html
task-list.html
task-form.html
task-details.html
```

Use Bootstrap for basic styling.

Do not add:

- React
- Angular
- Vue
- frontend build systems

JavaScript should be minimal and only used when necessary.

---

## Repository Rules

Use Spring Data JPA.

Prefer:

```java
JpaRepository
```

Use derived query methods when possible.

Avoid custom SQL unless necessary.

---

## Service Layer Rules

Business logic belongs in services.

`TaskService` is responsible for:

- creating tasks
- retrieving tasks
- updating tasks
- deleting tasks
- marking tasks complete
- calling `AiService`
- applying automation rules

Do not move business rules into controllers.

---

## Controller Rules

Controllers should:

- receive HTTP requests
- validate input
- call services
- populate `Model`
- return Thymeleaf view names
- redirect after successful form submissions

Controllers should not:

- contain database logic
- contain AI integration code
- contain large business rules

---

## AI Integration Rules

Initially, use a mock implementation.

Do not connect a real AI API until normal CRUD works.

When real AI is added:

- use Spring AI
- keep the existing `TaskService` API unchanged
- keep AI-specific code inside `AiService`
- never hardcode API keys
- use environment variables or external configuration
- handle API failure gracefully
- ensure tasks can still be created if AI is unavailable

Fallback values:

```text
category = Uncategorized
priority = MEDIUM
summary = AI analysis unavailable
```

---

## Database Rules

Use H2 first.

Switch to MySQL later.

Never commit:

- production passwords
- API keys
- secrets

Prefer environment variables for sensitive values.

---

## Validation Rules

At minimum:

```text
title must not be blank
description must not be blank
```

Add sensible length limits when appropriate.

Display validation errors in Thymeleaf.

---

## Security Rules

Do not implement security until the core application works.

First version:

```text
login
logout
USER role
```

Add ADMIN only if needed later.

Do not overcomplicate authentication.

---

## Testing Rules

After backend changes run:

```bash
./mvnw test
```

Also verify:

```bash
./mvnw package
```

Do not finish a task with compilation or test failures.

Important tests include:

- task creation
- task update
- task deletion
- mark completed
- HIGH priority becomes NEEDS_ATTENTION
- LOW/MEDIUM priority becomes OPEN
- AI failure still saves the task

---

## Implementation Order

Follow this sequence unless explicitly instructed otherwise:

```text
1. Task entity
2. TaskRepository
3. TaskService
4. TaskController
5. Thymeleaf CRUD pages
6. H2 CRUD verification
7. CSS / Bootstrap
8. Mock AiService
9. AI integration into task creation
10. Simple automation
11. Real AI provider
12. MySQL
13. Dashboard
14. Spring Security
15. Validation improvements
16. Tests
17. README
```

Do not jump ahead unnecessarily.

---

## Codex Working Style

Before making a large change:

1. Inspect the existing code.
2. Identify the files that need modification.
3. Prefer the smallest change that satisfies the requirement.
4. Preserve existing working APIs unless change is necessary.

After making a change:

1. Run relevant tests.
2. Run the Maven build when appropriate.
3. Fix compilation errors.
4. Summarize files changed.
5. Explain why each change was needed.
6. Explain the request flow in beginner-friendly terms.

---

## Learning Requirement

The repository is also a learning project.

When implementing a Spring Boot feature, explain important concepts such as:

- `@Controller`
- `@Service`
- `@Repository`
- `@Entity`
- `@Id`
- `@GeneratedValue`
- `JpaRepository`
- constructor injection
- `Model`
- Thymeleaf form binding
- validation
- request mappings
- JPA persistence

Do not merely generate code when an explanation would help understanding.

---

## Interview Preparation

When asked to explain code, use the actual classes and request flow from this repository.

Prefer explanations in this form:

```text
Browser
   ->
Controller
   ->
Service
   ->
Repository
   ->
Database
```

For AI:

```text
TaskService
   ->
AiService
   ->
AI Model
```

Explain code at fresher interview level unless deeper detail is requested.

---

## Scope Restrictions

Do not add the following unless explicitly requested:

- React
- Angular
- Vue
- Microservices
- Kafka
- Redis
- RabbitMQ
- Kubernetes
- Spring Cloud
- WebFlux
- GraphQL
- Complex workflow engines
- Vector databases
- Embeddings
- RAG
- Multiple AI providers
- Event-driven architecture
- Unnecessary design patterns

The project should stay small, readable, and interview-friendly.

---

## Definition of Done

A feature is complete when:

- it compiles
- relevant tests pass
- the Maven build succeeds
- the implementation follows the layered architecture
- no secrets are hardcoded
- no unnecessary technologies were introduced
- the feature is simple enough to explain in an interview
