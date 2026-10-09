# Student Management System with Placement Eligibility Engine

A full-stack student and placement management application built with **Java, Spring Boot, PostgreSQL, and a lightweight HTML/CSS/JavaScript frontend**. It manages student academic records and evaluates placement eligibility against configurable, company-specific criteria.

The eligibility decision is calculated by the Spring Boot backend. Each result includes the decision and the reasons the student meets or fails the selected company's requirements.

## Features

- **Dashboard:** Student and placement overview, including eligibility counts derived from backend data.
- **Student management:** Register, view, search, update, and delete student records.
- **Placement eligibility engine:** Evaluate a student against one company or all configured companies.
- **Configurable placement criteria:** Create, view, update, and delete company requirements.
- **Validation and error handling:** Request validation, duplicate-record handling, and structured API error responses.
- **Sample data seeding:** Optional starter records are inserted when the relevant tables are empty.
- **Automated tests:** JUnit 5 and Mockito tests for service logic and student controller behavior.

## Technology Stack

| Layer | Technologies |
|---|---|
| Language | Java 17 (the project may also run on newer compatible JDKs) |
| Backend | Spring Boot 3.3.5, Spring Web |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation |
| Frontend | HTML5, CSS3, vanilla JavaScript, Fetch API |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test |

## Architecture

```text
Browser (HTML / CSS / JavaScript)
              |
         Fetch API / HTTP
              |
   Spring Boot REST Controllers
              |
           Services
       (business logic)
              |
      Spring Data JPA
              |
          PostgreSQL
```

The frontend is served by Spring Boot from `src/main/resources/static/`. A separate frontend server or Node.js build step is not required.

## Project Structure

```text
student-placement-system/
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   ├── java/com/placement/sms/
    │   │   ├── config/        # Sample-data seeding
    │   │   ├── controller/    # Student, criteria, and eligibility APIs
    │   │   ├── dto/           # Request and response models
    │   │   ├── entity/        # JPA entities and status enums
    │   │   ├── exception/     # Application exceptions and error handling
    │   │   ├── repository/    # Spring Data JPA repositories
    │   │   ├── service/       # Student, criteria, and eligibility logic
    │   │   └── StudentPlacementApplication.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/
    │               ├── api.js
    │               └── app.js
    └── test/java/com/placement/sms/
        ├── controller/StudentControllerTest.java
        └── service/
            ├── EligibilityServiceTest.java
            └── StudentServiceTest.java
```

## Prerequisites

Install the following before running the application:

- **JDK 17 or newer**
- **Maven 3.9 or newer**
- **PostgreSQL 13 or newer**

Check your Java and Maven installations:

```powershell
java -version
mvn -version
```

## Run Locally on Windows

### 1. Create the database

If you have not already created the database, open `psql` and run:

```sql
CREATE DATABASE student_placement_db;
```

If the database already exists, do not create it again. The application connects to PostgreSQL on `localhost:5432` by default.

### 2. Configure database credentials

Open a terminal in the project root (the folder containing `pom.xml`) and copy the example environment file:

```powershell
Copy-Item .env.example .env
```

Edit `.env` with the credentials for your local PostgreSQL instance:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=student_placement_db
DB_USERNAME=postgres
DB_PASSWORD=YOUR_ACTUAL_POSTGRES_PASSWORD
SERVER_PORT=8080
APP_SEED_ENABLED=true
```

Replace `YOUR_ACTUAL_POSTGRES_PASSWORD` with your real PostgreSQL password. The username and password must match your local PostgreSQL configuration.

**Security:** `.env` contains local credentials and must not be committed to Git. The repository's `.gitignore` should exclude it.

### 3. Run tests

```powershell
mvn clean test
```

The unit tests use mocks and are designed not to require a running database.

### 4. Start the application

```powershell
mvn spring-boot:run
```

Wait until the logs report that Tomcat has started and `StudentPlacementApplication` has started successfully.

### 5. Open the application

- **Frontend:** http://localhost:8080/
- **Students API:** http://localhost:8080/api/students
- **Placement criteria API:** http://localhost:8080/api/criteria

Keep the terminal running while using the application. Stop the server with `Ctrl+C`.

## Frontend Sections

| Section | Purpose |
|---|---|
| Dashboard | Summarizes student and placement eligibility information. |
| Students | Lists and searches students, with registration, editing, and deletion actions. |
| Eligibility | Checks a student's eligibility for a selected company or against all configured criteria. |
| Placement Criteria | Manages company-specific CGPA and active-backlog requirements. |

The UI calls the backend APIs. It does not connect directly to PostgreSQL or independently implement the eligibility rules.

## Placement Eligibility Rules

A student is eligible for a company only when **both** conditions are satisfied:

1. Student CGPA is greater than or equal to the company's minimum CGPA.
2. Student active-backlog count is less than or equal to the company's maximum permitted active backlogs.

The backend returns an eligibility status and explanatory reasons for the result. Criteria can differ between companies.

## REST API Reference

Base URL: `http://localhost:8080`

### Students — `/api/students`

| Method | Endpoint | Description | Typical success response |
|---|---|---|---|
| `GET` | `/api/students` | List students | `200 OK` |
| `GET` | `/api/students/{id}` | Get one student | `200 OK` |
| `POST` | `/api/students` | Register a student | `201 Created` |
| `PUT` | `/api/students/{id}` | Update a student's details | `200 OK` |
| `DELETE` | `/api/students/{id}` | Delete a student | `204 No Content` |

Student request fields include `rollNumber`, `fullName`, `email`, `department`, `cgpa`, and `activeBacklogs`. `placementStatus`, `companyPlaced`, and `packageLpa` are also supported according to the request validation rules.

Example request to register a student:

```json
{
  "rollNumber": "MCA009",
  "fullName": "Ishaan Verma",
  "email": "ishaan.verma@example.com",
  "department": "MCA",
  "cgpa": 8.6,
  "activeBacklogs": 0
}
```

Send this JSON as the body of a `POST` request to `/api/students` with `Content-Type: application/json`.

### Placement Criteria — `/api/criteria`

| Method | Endpoint | Description | Typical success response |
|---|---|---|---|
| `GET` | `/api/criteria` | List company criteria | `200 OK` |
| `GET` | `/api/criteria/{id}` | Get one criterion | `200 OK` |
| `POST` | `/api/criteria` | Create a criterion | `201 Created` |
| `PUT` | `/api/criteria/{id}` | Update a criterion | `200 OK` |
| `DELETE` | `/api/criteria/{id}` | Delete a criterion | `204 No Content` |

Criteria request fields are `companyName`, `minCgpa`, and `maxActiveBacklogs`.

Example request:

```json
{
  "companyName": "Example Technologies",
  "minCgpa": 7.5,
  "maxActiveBacklogs": 0
}
```

### Eligibility — `/api/eligibility`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/eligibility/students/{studentId}` | Check one student against all configured companies |
| `GET` | `/api/eligibility/students/{studentId}/criteria/{criteriaId}` | Check one student against a specific company's criteria |

### Error responses

The API uses appropriate HTTP status codes for successful requests and common failures, including:

- `400 Bad Request` — validation or malformed request errors.
- `404 Not Found` — requested student, criterion, or endpoint does not exist.
- `409 Conflict` — duplicate student roll number, email, or company name.
- `500 Internal Server Error` — unexpected server-side error.

Validation errors may include field-level details to help identify invalid input.

## Tests

Run all tests with:

```powershell
mvn clean test
```

The current test suite includes:

- `EligibilityServiceTest` — eligibility decisions, boundary cases, failure reasons, and missing resources.
- `StudentServiceTest` — student creation, normalization, duplicate checks, update, delete, and missing records.
- `StudentControllerTest` — HTTP responses and controller-level error handling.

## Troubleshooting

| Problem | What to check |
|---|---|
| Missing `DB_USERNAME` or `DB_PASSWORD` | Confirm `.env` exists in the project root and contains both variables. |
| PostgreSQL authentication failure | Check the username and password against your local PostgreSQL account. |
| Connection refused | Confirm PostgreSQL is running and the host/port are correct. |
| Port 8080 is already in use | Set `SERVER_PORT=8081` in `.env` and open `http://localhost:8081/`. |
| Sample data does not appear | The seeder only inserts sample records when the relevant tables are empty; check `APP_SEED_ENABLED`. |
| Browser shows an API error | Check the running backend logs and the browser's Developer Tools → Network tab. |

## Security and Production Notes

- Do not commit `.env` or expose database credentials.
- This project does not include authentication or authorization; do not expose it publicly with real student data without adding appropriate access controls.
- Hibernate schema auto-update is configured for development. For production, use a database migration strategy and review database, logging, and security settings.

## License

No license is specified in this repository. Add a license file if you intend to define permissions for reuse and distribution.
