# SmartTask AI - Implementation Guide

## 1. Project Overview

SmartTask AI is a beginner-friendly task management application built with Spring Boot.

The application allows users to create, view, update, delete, and complete tasks.

AI is used only for task analysis. When a user creates a task, the application sends the task title and description to an AI service. The AI returns:

- Category
- Priority
- Short summary

The application then stores the task and AI-generated values in the database.

The project is intentionally simple so it is easy to understand and explain in a fresher interview.

---

## 2. Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring AI

### Frontend

- Thymeleaf
- HTML
- CSS
- Bootstrap

### Database

- H2 Database for early development
- MySQL for the final application

### AI

Use only one provider:

- Google Gemini through Spring AI

or

- OpenAI through Spring AI

Do not integrate multiple AI providers.

---

## 3. Spring Initializr Configuration

Create the project using Spring Initializr.

Use:

- Project: Maven
- Language: Java
- Spring Boot: 4.1.1
- Group: com.smarttask
- Artifact: smarttask-ai
- Name: SmartTask AI
- Description: AI-powered task management application
- Package name: com.smarttask
- Packaging: Jar
- Java: 17

### Dependencies

Add:

- Spring Web
- Thymeleaf
- Spring Data JPA
- Validation
- Spring Security
- H2 Database
- MySQL Driver
- Spring Boot DevTools

Add the Spring AI dependency later when normal CRUD is working.

---

## 4. Project Architecture

Keep the architecture simple.

```text
Browser
   |
   v
TaskController
   |
   v
TaskService
   |
   +------> AiService
   |
   v
TaskRepository
   |
   v
Database
```

The responsibilities are:

### Controller

Handles HTTP requests and returns Thymeleaf pages.

### Service

Contains business logic.

### Repository

Handles database operations.

### AI Service

Communicates with the AI model and returns structured task analysis.

### Entity

Represents database tables.

### DTO

Transfers structured data between layers.

---

## 5. Recommended Package Structure

```text
src/main/java/com/smarttask/

├── SmartTaskApplication.java
│
├── controller/
│   └── TaskController.java
│
├── service/
│   ├── TaskService.java
│   └── AiService.java
│
├── repository/
│   └── TaskRepository.java
│
├── entity/
│   └── Task.java
│
├── dto/
│   └── TaskAiResult.java
│
├── config/
│
└── security/
```

Resources:

```text
src/main/resources/

├── templates/
│   ├── index.html
│   ├── task-list.html
│   ├── task-form.html
│   └── task-details.html
│
├── static/
│   └── css/
│       └── style.css
│
├── application.properties
│
└── data.sql
```

---

## 6. Task Entity

Create a `Task` entity with these fields:

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

Suggested types:

```java
Long id;
String title;
String description;
String category;
String priority;
String status;
String aiSummary;
LocalDateTime createdAt;
```

Suggested default status:

```text
OPEN
```

Suggested priority values:

```text
LOW
MEDIUM
HIGH
```

---

## 7. Task Repository

Create:

```java
public interface TaskRepository extends JpaRepository<Task, Long>
```

Useful methods:

```java
List<Task> findByStatus(String status);

List<Task> findByPriority(String priority);
```

Do not write custom SQL unless it is actually needed.

---

## 8. Task Service

`TaskService` should contain the application's main business logic.

Recommended methods:

```text
getAllTasks()
getTaskById()
createTask()
updateTask()
deleteTask()
markTaskCompleted()
```

Important rule:

Controllers must not directly call the repository.

Use:

```text
Controller
   ->
Service
   ->
Repository
```

---

## 9. Thymeleaf Pages

Create these pages first.

### Task List

Route:

```text
/tasks
```

Displays all tasks.

### Create Task

Route:

```text
/tasks/new
```

Contains a form with:

```text
Title
Description
```

The AI-generated fields should not initially be entered by the user.

### Task Details

Route:

```text
/tasks/{id}
```

Displays:

```text
Title
Description
Category
Priority
AI Summary
Status
Created Date
```

### Edit Task

Route:

```text
/tasks/{id}/edit
```

Allows normal task editing.

---

## 10. First Development Phase - CRUD Only

Do not connect AI immediately.

First complete:

```text
Create Task
Read Task
Update Task
Delete Task
Mark Task Completed
```

Development flow:

```text
HTML Form
   ->
TaskController
   ->
TaskService
   ->
TaskRepository
   ->
Database
```

Verify CRUD before adding AI.

---

## 11. Mock AI Service

Before connecting a real AI API, create a fake `AiService`.

Example:

```java
@Service
public class AiService {

    public TaskAiResult analyzeTask(String title, String description) {

        return new TaskAiResult(
                "Technical",
                "MEDIUM",
                "Task requires technical attention."
        );
    }
}
```

This allows the application flow to be completed before external API integration.

The flow becomes:

```text
Create Task
   ->
TaskService
   ->
AiService
   ->
TaskRepository
   ->
Database
```

---

## 12. AI Integration Point

The AI integration belongs inside `AiService`.

Do not call the AI model directly from the controller.

Correct:

```text
TaskController
   ->
TaskService
   ->
AiService
```

Incorrect:

```text
TaskController
   ->
AI API
```

The controller should remain responsible only for HTTP requests and page navigation.

---

## 13. Task AI Result DTO

Create:

```java
public record TaskAiResult(
        String category,
        String priority,
        String summary
) {
}
```

The AI should return structured data that can be mapped to this record.

Example response:

```json
{
  "category": "Payment",
  "priority": "HIGH",
  "summary": "Customers cannot complete payment transactions."
}
```

---

## 14. Real AI Integration

After mock AI works, replace only the implementation of `AiService`.

Keep the rest of the application unchanged.

Preferred flow:

```text
TaskService
   ->
AiService
   ->
Spring AI ChatClient
   ->
Gemini or OpenAI
```

Example conceptual code:

```java
TaskAiResult result = chatClient
        .prompt()
        .user("Analyze this task. Title: " + title + " Description: " + description)
        .call()
        .entity(TaskAiResult.class);
```

Never hardcode API keys.

Use environment variables or configuration properties.

---

## 15. AI Failure Handling

The application must still work when the AI service is unavailable.

If AI fails:

```text
category = "Uncategorized"
priority = "MEDIUM"
summary = "AI analysis unavailable"
```

The task should still be saved.

This demonstrates proper error handling.

---

## 16. Simple Automation Feature

Keep automation very simple.

Rule:

```text
If AI priority = HIGH
    status = NEEDS_ATTENTION
Else
    status = OPEN
```

Implement this rule inside `TaskService`.

Do not build a workflow engine.

Example:

```text
AI Result
   |
   v
Priority HIGH?
   |
   +-- Yes -> NEEDS_ATTENTION
   |
   +-- No  -> OPEN
```

This is enough to demonstrate automation.

---

## 17. Dashboard

After CRUD and AI integration work, add a dashboard.

Display:

```text
Total Tasks
High Priority Tasks
Open Tasks
Completed Tasks
Needs Attention
```

Keep it simple.

Suggested route:

```text
/
```

or:

```text
/dashboard
```

---

## 18. Spring Security

Add authentication only after the main application works.

Recommended first version:

```text
Login
Logout
Single USER role
```

Later, if needed:

```text
USER
ADMIN
```

Do not make security unnecessarily complicated.

---

## 19. Database Strategy

### Early Development

Use H2.

Benefits:

- No MySQL setup required
- Fast testing
- Easy debugging

### Final Project

Switch to MySQL.

Final properties should use environment variables for credentials where possible.

Never commit real passwords.

---

## 20. Validation

Validate task input.

Suggested rules:

```text
title
- required
- maximum reasonable length

description
- required
```

Use Jakarta Validation annotations.

Display validation messages in Thymeleaf.

---

## 21. Error Handling

Handle at least:

```text
Task not found
Invalid form input
Database errors
AI service failure
```

Do not show raw stack traces to users.

---

## 22. Testing

Add tests gradually.

Important tests:

```text
TaskService creates a task

HIGH AI priority sets status to NEEDS_ATTENTION

LOW or MEDIUM priority sets status to OPEN

AI failure still saves the task

Task can be marked completed
```

Run:

```bash
./mvnw test
```

Also verify:

```bash
./mvnw package
```

Fix build failures before considering a feature complete.

---

## 23. Recommended Implementation Order

Follow this order.

```text
1. Create project using Spring Initializr

2. Run generated project

3. Create Task entity

4. Create TaskRepository

5. Create TaskService

6. Create TaskController

7. Create Thymeleaf pages

8. Complete CRUD using H2

9. Add Bootstrap/CSS

10. Create mock AiService

11. Integrate AI analysis into createTask()

12. Add simple priority automation

13. Connect real AI provider

14. Configure MySQL

15. Add dashboard

16. Add Spring Security

17. Add validation

18. Add tests

19. Improve UI

20. Write README
```

Do not skip directly to AI integration.

---

## 24. Fresher Interview Explanation

The complete flow should be easy to explain:

```text
User submits task form

        ↓

TaskController receives request

        ↓

TaskService handles business logic

        ↓

AiService analyzes title and description

        ↓

AI returns category, priority and summary

        ↓

TaskService applies automation rule

        ↓

TaskRepository saves Task

        ↓

Database stores the task

        ↓

Controller displays result using Thymeleaf
```

A concise interview explanation:

> SmartTask AI is a Spring Boot task management application. I used Spring MVC and Thymeleaf for the web layer, Spring Data JPA for persistence, and MySQL for the database. When a task is created, the service layer calls an AI service that analyzes the task and returns a category, priority, and summary. I also added a simple automation rule where high-priority tasks are automatically marked as needing attention.

---

## 25. Scope Rules

Do not add the following unless explicitly needed:

- React
- Angular
- Microservices
- Kafka
- Redis
- Kubernetes
- Complex workflow engines
- Vector databases
- RAG
- WebFlux
- Spring Cloud
- Multiple AI providers
- Unnecessary design patterns

The goal is a small project that demonstrates:

```text
Java
Spring Boot
Spring MVC
JPA
Database
Thymeleaf
Security
AI Integration
Simple Automation
Testing
```

Every part of the project should be understandable and explainable by a fresher.
