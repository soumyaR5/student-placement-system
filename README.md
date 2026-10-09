# Student Management System with Placement Eligibility Engine

A Spring Boot REST API that manages student records (including CGPA and active backlogs) and decides
whether a student is eligible for a company, based on configurable company-specific placement criteria
(minimum CGPA and maximum active backlogs). Every eligibility result includes clear reasons.

**Stack:** Java 17, Spring Boot 3.3.5, Spring Data JPA (Hibernate), PostgreSQL, Maven, Jakarta Bean Validation, JUnit 5 + Mockito.

## Project structure

```
student-placement-system/
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   ├── java/com/placement/sms/
    │   │   ├── StudentPlacementApplication.java
    │   │   ├── config/        DataSeeder.java
    │   │   ├── controller/    StudentController, PlacementCriteriaController, EligibilityController
    │   │   ├── dto/           StudentRequest/Response, CriteriaRequest/Response, EligibilityResponse, EligibilityStatus
    │   │   ├── entity/        Student, AcademicRecord, PlacementInfo, PlacementStatus, PlacementCriteria
    │   │   ├── exception/     GlobalExceptionHandler, ErrorResponse, ResourceNotFound/Duplicate/InvalidRequest exceptions
    │   │   ├── repository/    StudentRepository, PlacementCriteriaRepository
    │   │   └── service/       StudentService, PlacementCriteriaService, EligibilityService, StudentMapper
    │   └── resources/
    │       ├── application.properties
    │       └── static/                  frontend, served at http://localhost:8080/
    │           ├── index.html
    │           ├── css/style.css
    │           └── js/api.js, app.js
    └── test/java/com/placement/sms/
        ├── controller/StudentControllerTest.java
        └── service/       EligibilityServiceTest, StudentServiceTest
```

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -version`)
- PostgreSQL 13+ (local install or Docker)

## 1. PostgreSQL setup

### Option A: local PostgreSQL

```bash
psql -U postgres
```

```sql
CREATE USER sms_user WITH PASSWORD 'change_me_securely';
CREATE DATABASE student_placement_db OWNER sms_user;
\q
```

### Option B: Docker

```bash
docker run --name sms-postgres \
  -e POSTGRES_USER=sms_user \
  -e POSTGRES_PASSWORD=change_me_securely \
  -e POSTGRES_DB=student_placement_db \
  -p 5432:5432 -d postgres:16
```

Tables are created automatically on first start (`spring.jpa.hibernate.ddl-auto=update`).

## 2. Configure credentials (environment based)

Database credentials are never stored in source code. `application.properties` reads them from
environment variables, and also loads a local `.env` file if one exists in the project root.

```bash
cp .env.example .env        # Windows: copy .env.example .env
```

Edit `.env`:

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `student_placement_db` | Database name |
| `DB_USERNAME` | *(required)* | Database user |
| `DB_PASSWORD` | *(required)* | Database password |
| `SERVER_PORT` | `8080` | HTTP port |
| `APP_SEED_ENABLED` | `true` | Insert sample data when tables are empty |

The application refuses to start if `DB_USERNAME` or `DB_PASSWORD` is missing. `.env` is listed in `.gitignore`.

Alternatively, export real environment variables instead of using `.env`:

```bash
# Linux / macOS
export DB_USERNAME=sms_user DB_PASSWORD=change_me_securely
# Windows PowerShell
$env:DB_USERNAME="sms_user"; $env:DB_PASSWORD="change_me_securely"
```

## 3. Build, test, run

Run all commands from the project root (where `pom.xml` and `.env` live).

**VS Code on Windows (PowerShell terminal):**

```powershell
cd path\to\student-placement-system
Copy-Item .env.example .env      # first time only, then edit .env
mvn clean test
mvn spring-boot:run
```

Then open http://localhost:8080/ in your browser.

```bash
mvn clean test              # run unit tests (no database needed)
mvn spring-boot:run         # start the API on http://localhost:8080
```

Or build a jar:

```bash
mvn clean package
java -jar target/student-placement-system-1.0.0.jar
```

## Frontend (web UI)

A minimal dashboard built with plain HTML, CSS and JavaScript (Fetch API). It lives in
`src/main/resources/static/` and is served by the same Spring Boot application, so there is no separate
server, no build step, and no CORS configuration.

**Open:** http://localhost:8080/ (after starting the app)

| Page | What it does | Backend calls |
|---|---|---|
| Dashboard | Total / eligible / not eligible / placed counts, recent students | `GET /api/students`, `GET /api/criteria`, `GET /api/eligibility/students/{id}` per student |
| Students | Table, client-side search by name or roll number, register, edit, delete (with confirmation) | `GET/POST /api/students`, `PUT/DELETE /api/students/{id}` |
| Eligibility | Pick a student (and optionally one company); shows Eligible / Not eligible with the backend's reasons | `GET /api/eligibility/students/{id}` and `.../criteria/{criteriaId}` |
| Placement Criteria | Table, add, edit, delete company criteria | `GET/POST /api/criteria`, `PUT/DELETE /api/criteria/{id}` |

Notes:

- Eligibility is always computed by the backend. The browser never re-implements the rules.
- On the dashboard, "Placement eligible" means eligible for at least one configured company, and
  "Not eligible" means eligible for none. It makes one eligibility request per student, which is fine at
  college scale; add a summary endpoint if the data grows large.
- Forms validate required fields and ranges before sending, block double submits, and show backend field
  errors (400) and conflicts (409) inline. Success messages appear only after the backend confirms.
- Data is never stored in the browser; every table is loaded from the API.

## Sample data

On the first start with empty tables, the app inserts (IDs assume a fresh database):

**Placement criteria**

| id | Company | Min CGPA | Max active backlogs |
|---|---|---|---|
| 1 | Google | 8.50 | 0 |
| 2 | Microsoft | 8.00 | 0 |
| 3 | TCS | 7.00 | 1 |
| 4 | Infosys | 6.50 | 2 |
| 5 | Zoho | 7.50 | 0 |

**Students**

| id | Roll no. | Name | CGPA | Backlogs | Placement |
|---|---|---|---|---|---|
| 1 | 21MCA001 | Aarav Sharma | 8.75 | 0 | NOT_PLACED |
| 2 | 21MCA002 | Priya Nair | 9.10 | 0 | PLACED (TCS, 7.00 LPA) |
| 3 | 21MCA003 | Rohan Mehta | 7.20 | 1 | NOT_PLACED |
| 4 | 21MCA004 | Sneha Iyer | 8.05 | 0 | NOT_PLACED |
| 5 | 21MCA005 | Karthik Reddy | 6.80 | 2 | NOT_PLACED |
| 6 | 21MCA006 | Divya Menon | 7.95 | 0 | NOT_PLACED |
| 7 | 21MCA007 | Arjun Patel | 8.40 | 1 | NOT_PLACED |
| 8 | 21MCA008 | Meera Krishnan | 9.35 | 0 | PLACED (Zoho, 12.00 LPA) |

## Eligibility rule

A student is **ELIGIBLE** for a company only if both conditions hold:

1. `student CGPA >= criteria minCgpa`
2. `student activeBacklogs <= criteria maxActiveBacklogs`

Otherwise the student is **INELIGIBLE**, and the `reasons` list explains which rule(s) failed.
Both rules are always reported, so the response shows the full picture.

## API reference

Base URL: `http://localhost:8080`

### Students: `/api/students`

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/api/students` | Register a student | `201 Created` + `Location` header |
| GET | `/api/students` | List all students | `200 OK` |
| GET | `/api/students/{id}` | Get one student | `200 OK` |
| PUT | `/api/students/{id}` | Replace a student's details | `200 OK` |
| DELETE | `/api/students/{id}` | Delete a student | `204 No Content` |

Request body (POST and PUT):

| Field | Type | Rules |
|---|---|---|
| `rollNumber` | string | required, max 30, unique (stored upper-case) |
| `fullName` | string | required, max 100 |
| `email` | string | required, valid email, unique (stored lower-case) |
| `department` | string | required, max 100 |
| `cgpa` | decimal | required, 0.00 to 10.00, max 2 decimals |
| `activeBacklogs` | integer | required, 0 to 50 |
| `placementStatus` | enum | optional: `NOT_PLACED` (default) or `PLACED` |
| `companyPlaced` | string | required when `PLACED`, max 100 |
| `packageLpa` | decimal | optional, >= 0, max 2 decimals |

### Placement criteria: `/api/criteria`

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/api/criteria` | Create company criteria | `201 Created` |
| GET | `/api/criteria` | List all criteria | `200 OK` |
| GET | `/api/criteria/{id}` | Get one | `200 OK` |
| PUT | `/api/criteria/{id}` | Update | `200 OK` |
| DELETE | `/api/criteria/{id}` | Delete | `204 No Content` |

Request body: `companyName` (required, unique ignoring case), `minCgpa` (0.00 to 10.00), `maxActiveBacklogs` (0 to 50).

### Eligibility: `/api/eligibility`

| Method | Path | Description |
|---|---|---|
| GET | `/api/eligibility/students/{studentId}/criteria/{criteriaId}` | Check one student against one company |
| GET | `/api/eligibility/students/{studentId}` | Check one student against all companies |

### Status codes

| Code | When |
|---|---|
| 200 / 201 / 204 | Success |
| 400 | Validation failed, malformed JSON, bad enum or path value, `PLACED` without `companyPlaced` |
| 404 | Student, criteria or endpoint not found |
| 405 / 415 | Unsupported method / media type |
| 409 | Duplicate roll number, email or company name |
| 500 | Unexpected server error (details are logged, not returned) |

Error body:

```json
{
  "timestamp": "2026-10-09T10:15:30.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/students",
  "fieldErrors": { "cgpa": "cgpa must be at most 10.00" }
}
```

## 4. Try it with curl

Postman users: **Import → Raw text** and paste any curl command below.

```bash
# Register a student
curl -i -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"rollNumber":"21MCA009","fullName":"Ishaan Verma","email":"ishaan.verma@example.com","department":"MCA","cgpa":8.60,"activeBacklogs":0}'

# List / get
curl http://localhost:8080/api/students
curl http://localhost:8080/api/students/1

# Update (full replace)
curl -X PUT http://localhost:8080/api/students/5 \
  -H "Content-Type: application/json" \
  -d '{"rollNumber":"21MCA005","fullName":"Karthik Reddy","email":"karthik.reddy@example.com","department":"MCA","cgpa":7.10,"activeBacklogs":0}'

# Delete
curl -i -X DELETE http://localhost:8080/api/students/9

# Create placement criteria
curl -i -X POST http://localhost:8080/api/criteria \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Amazon","minCgpa":8.25,"maxActiveBacklogs":0}'

# List criteria
curl http://localhost:8080/api/criteria

# Eligibility: Rohan (id 3) vs Google (id 1)
curl http://localhost:8080/api/eligibility/students/3/criteria/1

# Eligibility: Aarav (id 1) against all companies
curl http://localhost:8080/api/eligibility/students/1

# Validation error (expect 400)
curl -i -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"rollNumber":"","fullName":"X","email":"bad","department":"MCA","cgpa":12,"activeBacklogs":-1}'

# Not found (expect 404)
curl -i http://localhost:8080/api/students/9999

# Duplicate roll number (expect 409)
curl -i -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"rollNumber":"21MCA001","fullName":"Dup","email":"dup@example.com","department":"MCA","cgpa":8,"activeBacklogs":0}'
```

Windows `cmd`: put each command on one line and escape inner quotes as `\"`, or use Postman.

### Example: ineligible (two reasons)

`GET /api/eligibility/students/3/criteria/1`

```json
{
  "studentId": 3,
  "studentName": "Rohan Mehta",
  "rollNumber": "21MCA003",
  "criteriaId": 1,
  "companyName": "Google",
  "status": "INELIGIBLE",
  "eligible": false,
  "reasons": [
    "CGPA 7.20 is below the minimum requirement of 8.50",
    "Active backlogs 1 exceed the maximum allowed of 0"
  ]
}
```

### Example: eligible

`GET /api/eligibility/students/1/criteria/1`

```json
{
  "studentId": 1,
  "studentName": "Aarav Sharma",
  "rollNumber": "21MCA001",
  "criteriaId": 1,
  "companyName": "Google",
  "status": "ELIGIBLE",
  "eligible": true,
  "reasons": [
    "CGPA 8.75 meets the minimum requirement of 8.50",
    "Active backlogs 0 are within the maximum allowed of 0"
  ]
}
```

## 5. Tests

```bash
mvn clean test
```

| Test class | Covers |
|---|---|
| `EligibilityServiceTest` | eligible, exact-boundary, low CGPA, too many backlogs, both failing, missing student/criteria, check-all |
| `StudentServiceTest` | create + normalisation, duplicate roll/email, PLACED without company, not found, update, delete |
| `StudentControllerTest` | 201 + Location, 400 with field errors, 404 error body, 204 on delete |

Unit tests use mocks and need no database.

## 6. Upload to GitHub

```bash
cd student-placement-system
git init
git add .
git status                      # confirm .env and target/ are NOT listed
git commit -m "Initial commit: Student Management System with Placement Eligibility Engine"
git branch -M main
git remote add origin https://github.com/<your-username>/student-placement-system.git
git push -u origin main
```

Create the empty repository on GitHub first (no README or .gitignore, since they already exist locally).

## Troubleshooting

| Problem | Fix |
|---|---|
| `Could not resolve placeholder 'DB_USERNAME'` | `.env` is missing or you are not running from the project root; set the variables |
| `password authentication failed` | Check `DB_USERNAME` / `DB_PASSWORD` match the PostgreSQL user |
| `Connection refused` | PostgreSQL is not running or `DB_HOST` / `DB_PORT` is wrong |
| `Port 8080 already in use` | Set `SERVER_PORT=8081` in `.env` |
| Sample data not inserted | Tables were not empty, or `APP_SEED_ENABLED=false` |

## Design notes

- `ddl-auto=update` suits local development. For production, use a migration tool such as Flyway and switch to `validate`.
- There is no authentication by design (out of scope). Do not expose this API publicly as is.
