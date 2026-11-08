# Backend Integration Test Report

## 1. Project overview

The project is a Java 21 / Spring Boot 4.1.2 Maven application using Spring MVC, Spring Data JPA, Hibernate, MySQL in the default runtime configuration, and H2 for tests. The production configuration points to `student_management_db` through the `DB_PASSWORD` environment variable. The automated test run used only H2. The manual MySQL/Postman checks recorded in this report were previously completed by the user; no additional database operation was performed during this documentation review.

The automated integration suite uses the existing test-scope H2 configuration:

- JDBC URL: `jdbc:h2:mem:attendance;MODE=MySQL;DB_CLOSE_DELAY=-1`
- Schema mode: `create-drop`
- Test transaction rollback: enabled on the new integration base class
- Automated MySQL integration: not run
- Previously completed manual MySQL/Postman checks: listed separately below

## 2. Existing module audit

### Implemented modules

- Student management: `StudentController` and `StudentRepository`
- Subject management: `SubjectController`, `SubjectService`, and `SubjectRepository`
- Student-subject assignment: `StudentSubjectController`, `StudentSubjectService`, and `StudentSubjectRepository`
- Attendance: `AttendanceController`, `AttendanceService`, and `AttendanceRepository`
- Marks: `StudentMarkController`, `StudentMarkService`, and `StudentMarkRepository`
- Student performance summary: implemented by `StudentMarkService.getStudentPerformance`
- Centralized error responses: `GlobalExceptionHandler` and `ApiError`

There is no separate `StudentService` class in the current code. Student CRUD is implemented directly by `StudentController` through `StudentRepository`. This was preserved because it is existing working behavior.

### Relationships audited

- `StudentSubject.student` -> `Student`, many-to-one, non-null, lazy
- `StudentSubject.subject` -> `Subject`, many-to-one, non-null, lazy
- `StudentMark.student` -> `Student`, many-to-one, non-null, lazy
- `StudentMark.subject` -> `Subject`, many-to-one, non-null, lazy
- `Attendance.student` -> `Student`, many-to-one, non-null, lazy
- `Attendance.subject` -> `Subject`, many-to-one, non-null, lazy
- Unique assignment constraint: student + subject
- Unique mark constraint: student + subject + assessment name + assessment type
- Unique attendance constraint: student + subject + attendance date

### Observed risks and limitations

1. Attendance search loads all attendance rows and filters/sorts in memory. This is correct for the tested data but is a scalability and possible N+1/performance concern for a large database.
2. The application logs SQL (`spring.jpa.show-sql=true`) in the default configuration. This can be noisy and should be reviewed for production logging policy.
3. Spring reports Open Session in View as enabled. This can permit lazy-loading queries during response rendering and should be made an explicit deployment decision.
4. No authentication or authorization configuration was found. The requested tests therefore validate unauthenticated API behavior and do not claim security control coverage.
5. No explicit CORS configuration was found. Browser-origin CORS behavior was not claimed as tested.
6. No approved isolated MySQL test database or Testcontainers MySQL setup was present. H2 tests do not prove MySQL compatibility.
7. Pagination is implemented for attendance only. Student, subject, assignment, and marks list endpoints return unpaged arrays.
8. Subject deletion is explicitly blocked while an assignment exists. The safe workflow removes marks, attendance, and assignment before deleting the subject; the student remains intact.

No duplicate business logic, incorrect relationship mapping, or production validation defect requiring a production code change was identified by the executed tests.

## 3. Complete API endpoint matrix

| Module | Endpoint | Automated H2/MockMvc coverage |
|---|---|---|
| Students | `POST /api/students` | create, validation, duplicate email |
| Students | `GET /api/students` | retrieval |
| Students | `GET /api/students/{id}` | retrieval and 404 |
| Students | `PUT /api/students/{id}` | update, duplicate email, 404 behavior |
| Students | `DELETE /api/students/{id}` | delete and 404 behavior |
| Subjects | `POST /api/subjects` | create and credits validation |
| Subjects | `GET /api/subjects` | retrieval |
| Subjects | `GET /api/subjects/{id}` | retrieval and 404 |
| Subjects | `PUT /api/subjects/{id}` | update and duplicate code |
| Subjects | `DELETE /api/subjects/{id}` | delete and assigned-subject conflict |
| Assignment | `POST /api/students/{studentId}/subjects/{subjectId}` | create, duplicate, invalid student |
| Assignment | `GET /api/students/{studentId}/subjects` | relationship retrieval |
| Assignment | `GET /api/subjects/{subjectId}/students` | relationship retrieval |
| Assignment | `DELETE /api/students/{studentId}/subjects/{subjectId}` | removal and parent preservation |
| Attendance | `POST /api/attendance` | create, statuses, future date, duplicate |
| Attendance | `GET /api/attendance` | filters, page, size, sort |
| Attendance | `GET /api/attendance/{id}` | direct retrieval and 404 |
| Attendance | `PUT /api/attendance/{id}` | update |
| Attendance | `DELETE /api/attendance/{id}` | delete |
| Attendance | `GET /api/attendance/student/{studentId}` | direct ordered-list retrieval |
| Attendance | `GET /api/attendance/subject/{subjectId}` | direct ordered-list retrieval |
| Attendance | `GET /api/attendance/date/{date}` | direct date retrieval |
| Attendance | `GET /api/attendance/range` | valid inclusive range and invalid range |
| Attendance | `GET /api/attendance/summary/student/{studentId}` | counts and percentage |
| Attendance | `GET /api/attendance/summary/subject/{subjectId}` | direct counts and percentage |
| Marks | `POST /api/marks` | create, assignment requirement, validation |
| Marks | `GET /api/marks` | retrieval |
| Marks | `GET /api/marks/{id}` | retrieval and 404 |
| Marks | `PUT /api/marks/{id}` | update and recalculation |
| Marks | `DELETE /api/marks/{id}` | delete |
| Marks | `GET /api/marks/student/{studentId}` | student filter and invalid student |
| Marks | `GET /api/marks/subject/{subjectId}` | subject filter |
| Marks | `GET /api/marks/student/{studentId}/subject/{subjectId}` | combined filter |
| Performance | `GET /api/marks/student/{studentId}/performance` | weighted calculation, empty response, 404, grade boundaries |

## 4. Automated test implementation

### Test classes added

- `IntegrationTestSupport`: shared Spring Boot, MockMvc, ObjectMapper, H2, and transaction support
- `BackendApiIntegrationTest`: student, subject, assignment, and attendance API coverage
- `MarksAndPerformanceIntegrationTest`: marks, performance, validation, weighted totals, and grade boundaries
- `CompleteStudentManagementWorkflowTest`: complete workflow and direct repository relationship verification
- `EndpointCoverageIntegrationTest`: remaining attendance routes, filters, invalid requests, subject summaries, and timestamp persistence

### Existing test classes retained

Existing unit and controller tests were not replaced or deleted. Existing service unit tests and the existing standalone MockMvc student controller test remain in place.

## 5. Previously completed manual MySQL/Postman checks

The following checks are recorded as **manual verification**, not automated MySQL integration tests:

- Subject creation and retrieval
- Student-subject assignment
- Marks creation, update, and MySQL persistence
- Attendance creation, retrieval, and MySQL persistence
- Student attendance summary: one PRESENT record returned 100%
- Student performance summary: 95/100 returned 95% and grade A+

The complete Postman collection has **not** been executed as a collection. Its JSON syntax was validated, but syntax validation is not API execution evidence.

All other real-MySQL endpoint scenarios remain **NOT RUN**. The automated PASS results below refer to H2/MockMvc only.

## 6. Performance calculation verification

The executed performance test uses assessments with 34 obtained marks out of 40 maximum marks:

`(34 / 40) * 100 = 85.0000%`

The endpoint returned:

- Student ID and full name
- Assessment count: 3
- Total obtained: 34
- Total maximum: 40
- Overall average percentage: 85.0
- Overall grade: A
- Subject assessment count and weighted subject totals

The test verifies the service sums marks and maximum marks before dividing. It does not average assessment percentages with equal weight. Students with no marks return zero totals, an empty subject list, and grade `N/A`. A missing student returns 404.

## 7. Attendance calculation verification

The attendance service counts `PRESENT`, `ABSENT`, and `LATE` records and treats PRESENT plus LATE as attended:

`(presentDays + lateDays) / totalDays * 100`

The executed student and subject summary tests verified three records: one PRESENT, one ABSENT, and one LATE. Both summaries returned one day in each category and `66.67` percent. Empty student attendance returned zero days and zero percent. Student, subject, date, range, status, and combined query filters were also exercised.

## 8. Grade boundary verification

The performance endpoint was exercised at every requested boundary: 95, 90, 89, 80, 79, 70, 69, 60, 59, 50, 49, and 0.

| Percentage | Expected and observed grade |
|---:|:---|
| 95 | A+ |
| 90 | A+ |
| 89 | A |
| 80 | A |
| 79 | B |
| 70 | B |
| 69 | C |
| 60 | C |
| 59 | D |
| 50 | D |
| 49 | F |
| 0 | F |

## 9. Actual test results

Latest verified execution:

- Date: 2026-11-08
- Java runtime: 24.0.1
- Compilation target: Java 21
- Spring Boot: 4.1.2
- Test database: H2 (`jdbc:h2:mem:attendance`)
- Execution time: 32.036 seconds

Commands executed:

```text
  .\mvnw.cmd clean test
```

Results:

- Maven build: BUILD SUCCESS
- Automated tests: 58 executed
- Passed: 58
- Failed: 0
- Errors: 0
- Skipped: 0
- H2/MockMvc automated integration tests: passed
- Automated MySQL integration tests: not run
- Previously completed manual MySQL/Postman checks: limited to the cases listed in section 5
- Complete Postman collection execution: not run; JSON syntax validated only

The test logs explicitly showed the H2 in-memory URL. During this documentation review, no `DROP DATABASE`, `DROP TABLE`, `TRUNCATE`, destructive SQL, password change, or deletion of existing project data was performed.

Surefire inspection found 11 XML reports under `target/surefire-reports` with the same totals: 58 tests, 0 failures, 0 errors, and 0 skipped.

## 10. API response and security checks

Automated coverage verifies status codes for successful, validation, conflict, and not-found cases; JSON response fields; error response status/message/path shape; and absence of a `trace` field in a Postman not-found assertion. The global handler returns structured `ApiError` responses and does not expose stack traces in its defined handlers.

No credentials were added to the Postman environment. The production password remains `${DB_PASSWORD}` in `application.properties`.

CORS and authentication were not introduced or claimed because no existing project configuration for either was found.

## 11. Postman assets

- Collection: `postman/Student_Management_System_Integration_Tests.postman_collection.json`
- Environment: `postman/Student_Management_Local.postman_environment.json`

The collection contains the requested folders:

1. Student Management
2. Subject Management
3. Student–Subject Assignment
4. Attendance Management
5. Marks Management
6. Student Performance
7. Validation Testing
8. End-to-End Workflow

It captures IDs from create responses, asserts status and response shape, checks percentage/grade behavior, and performs cleanup in dependency-safe order. It does not contain a database password. The collection has not been executed end-to-end.

## 12. Fixes applied

- Added isolated integration-test support and rollback-scoped H2 integration tests.
- Added API, persistence, workflow, validation, weighted-performance, and grade-boundary coverage.
- Added the Postman collection and environment.
- Added `docs/API_ENDPOINT_TEST_CHECKLIST.md` with endpoint-level expected and actual results.
- Added this report.
- No production source code or business logic was changed.

## 13. Final backend integration status

**Automated H2 backend integration status: PASS — 58/58 tests passed.**

**Automated MySQL integration status: NOT RUN.** The 58 automated tests do not establish MySQL compatibility. Previously completed manual MySQL checks are limited to the cases listed in section 5; no broader MySQL endpoint coverage is claimed.

**Postman status: PARTIALLY MANUALLY VERIFIED / COLLECTION NOT RUN.** The supplied manual checks are recorded in section 5. The complete collection is syntax-valid but has not been executed as a collection.

Remaining limitations are the absence of automated MySQL integration, incomplete manual MySQL endpoint coverage, unexecuted complete Postman collection, lack of authentication/CORS coverage, and the attendance service's in-memory filtering/pagination implementation for large datasets.
