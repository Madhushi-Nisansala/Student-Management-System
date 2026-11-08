# API Endpoint Integration Test Checklist

Generated from the current controller mappings and the executed H2/MockMvc test suite.

## Result legend

- **PASS — H2/MockMvc**: endpoint was exercised by automated tests against the isolated H2 database.
- **MANUAL VERIFIED — MySQL/Postman**: manually verified previously by the user; not an automated MySQL result.
- **MANUAL — NOT RUN**: available in the Postman collection but not executed as part of the complete collection.
- **NOT RUN — MySQL**: no real-MySQL verification evidence was supplied for this endpoint.
- **N/A**: the requested capability is not implemented by the current API.

Latest automated command: `.\mvnw.cmd clean test` — Maven `BUILD SUCCESS`.

- Date: 2026-11-08
- Java runtime: 24.0.1
- Compilation target: Java 21
- Spring Boot: 4.1.2
- Test database: H2 (`jdbc:h2:mem:attendance`)
- Execution time: 32.036 seconds

The latest automated suite result was 58 tests passed, 0 failures, 0 errors, and 0 skipped. The automated integration tests used H2, not MySQL.

## Student Management

| Method | Endpoint | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|---|
| POST | `/api/students` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/students` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/students/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| PUT | `/api/students/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| DELETE | `/api/students/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Student required fields/email | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Duplicate email | PASS — H2/MockMvc | NOT RUN — MySQL |
| Pagination/sorting | Student list | N/A | N/A |

## Subject Management

| Method | Endpoint | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|---|
| POST | `/api/subjects` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman |
| GET | `/api/subjects` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman |
| GET | `/api/subjects/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| PUT | `/api/subjects/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| DELETE | `/api/subjects/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Required fields and positive credits | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Duplicate subject code | PASS — H2/MockMvc | NOT RUN — MySQL |
| Pagination/sorting | Subject list | N/A | N/A |

## Student–Subject Assignment

| Method | Endpoint | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|---|
| POST | `/api/students/{studentId}/subjects/{subjectId}` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman |
| GET | `/api/students/{studentId}/subjects` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/subjects/{subjectId}/students` | PASS — H2/MockMvc | NOT RUN — MySQL |
| DELETE | `/api/students/{studentId}/subjects/{subjectId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Duplicate assignment | PASS — H2/MockMvc | NOT RUN — MySQL |
| Not found | Invalid student/subject/assignment | PASS — H2/MockMvc | NOT RUN — MySQL |

## Attendance Management

| Method | Endpoint | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|---|
| POST | `/api/attendance` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman |
| GET | `/api/attendance` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| PUT | `/api/attendance/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| DELETE | `/api/attendance/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/student/{studentId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/subject/{subjectId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/date/{date}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/range` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/attendance/summary/student/{studentId}` | PASS — H2/MockMvc | MANUAL VERIFIED — one PRESENT record returned 100% |
| GET | `/api/attendance/summary/subject/{subjectId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| Filter | student, subject, date, status | PASS — H2/MockMvc | NOT RUN — MySQL |
| Paging | page, size, sort | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Invalid enum/date/JSON/missing fields | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Future attendance date | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Duplicate student/subject/date | PASS — H2/MockMvc | NOT RUN — MySQL |
| Empty result | No attendance records | PASS — H2/MockMvc | NOT RUN — MySQL |

Attendance summary rule verified by the implementation and H2 tests:

```text
attendance percentage = (PRESENT + LATE) / total days * 100
```

The H2 test verified one PRESENT, one ABSENT, and one LATE as 66.67%. The separate manual MySQL check verified one PRESENT record as 100%.

## Marks Management

| Method | Endpoint | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|---|
| POST | `/api/marks` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman creation and persistence |
| GET | `/api/marks` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/marks/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| PUT | `/api/marks/{id}` | PASS — H2/MockMvc | MANUAL VERIFIED — MySQL/Postman update and persistence |
| DELETE | `/api/marks/{id}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/marks/student/{studentId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/marks/subject/{subjectId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/marks/student/{studentId}/subject/{subjectId}` | PASS — H2/MockMvc | NOT RUN — MySQL |
| GET | `/api/marks/student/{studentId}/performance` | PASS — H2/MockMvc | MANUAL VERIFIED — 95/100 = 95%, grade A+ |
| Validation | Negative marks | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Marks above maximum | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Zero/negative maximum | PASS — H2/MockMvc | NOT RUN — MySQL |
| Validation | Empty assessment name/type/date/JSON | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Duplicate assessment | PASS — H2/MockMvc | NOT RUN — MySQL |
| Conflict | Student not assigned to subject | PASS — H2/MockMvc | NOT RUN — MySQL |
| Not found | Invalid student/subject/mark | PASS — H2/MockMvc | NOT RUN — MySQL |
| Pagination/sorting | Marks lists | N/A | N/A |

## Calculation coverage

| Calculation | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|
| Individual assessment percentage | PASS — 9/10 = 90.0000% | NOT RUN — MySQL |
| Individual assessment grade | PASS — 90% = A+ | NOT RUN — MySQL |
| Subject weighted average | PASS — totals divided, not simple percentage average | NOT RUN — MySQL |
| Student weighted average | PASS — 34/40 = 85.0000%, grade A | MANUAL VERIFIED — 95/100 = 95%, grade A+ |
| Grade boundaries | PASS — all requested boundaries | NOT RUN — MySQL |
| Decimal rounding | PASS — four decimal places, HALF_UP | NOT RUN — MySQL |
| Zero maximum | PASS — 400 validation response | NOT RUN — MySQL |

## Database and response checks

| Check | Automated H2/MockMvc | Manual MySQL/Postman |
|---|---|---|
| H2 schema creation | PASS — isolated `create-drop` database | N/A |
| Student/subject persistence | PASS — repository-backed workflow assertions | Subject creation/retrieval manually verified; student persistence NOT RUN — MySQL |
| Assignment relationship | PASS — unique relationship and parent preservation | Assignment manually verified; broader relationship checks NOT RUN — MySQL |
| Attendance relationship/persistence | PASS — repository-backed IDs and timestamps | Attendance creation/retrieval/persistence manually verified |
| Marks relationship/persistence | PASS — repository-backed update and timestamps | Marks creation/update/persistence manually verified |
| Foreign-key/unique behavior | PASS — H2 constraints and service conflicts | NOT RUN — MySQL |
| Error response shape | PASS — status/message/path assertions | NOT RUN — complete Postman execution |
| Complete Postman collection | NOT RUN | JSON syntax valid; complete collection execution NOT RUN |
