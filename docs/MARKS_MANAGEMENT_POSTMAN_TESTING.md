# Marks Management API â€“ Postman Testing Guide

Base URL:

`http://localhost:8080`

Use the Postman environment variable `baseUrl` when creating requests:

`{{baseUrl}}`

For POST and PUT requests, add these headers:

| Header | Value |
|---|---|
| `Content-Type` | `application/json` |
| `Accept` | `application/json` |

## Important prerequisite

A student must already exist and must already be assigned to the subject before marks can be created. Use an existing Studentâ€“Subject assignment from the assignment API.

## 1. Add marks

**Method:** POST
**URL:** `{{baseUrl}}/api/marks`

**Body:**

```json
{
  "studentId": 1,
  "subjectId": 1,
  "assessmentName": "Quiz 1",
  "assessmentType": "QUIZ",
  "marksObtained": 18,
  "maximumMarks": 20,
  "assessmentDate": "2026-01-15",
  "remarks": "Good understanding of the topic"
}
```

`percentage` and `grade` are calculated by the backend. Do not send them in the request.

**Expected status:** `201 Created`

**Example response:**

```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "Kamal Perera",
  "subjectId": 1,
  "subjectCode": "CS101",
  "subjectName": "Programming Fundamentals",
  "assessmentName": "Quiz 1",
  "assessmentType": "QUIZ",
  "marksObtained": 18,
  "maximumMarks": 20,
  "percentage": 90.0000,
  "grade": "A+",
  "assessmentDate": "2026-01-15",
  "remarks": "Good understanding of the topic",
  "createdAt": "2026-01-15T10:00:00",
  "updatedAt": "2026-01-15T10:00:00"
}
```

## 2. Get all marks

**Method:** GET
**URL:** `{{baseUrl}}/api/marks`

**Expected status:** `200 OK`

The response is a JSON array. If no marks exist, the response is `[]`.

## 3. Get mark by ID

**Method:** GET
**URL:** `{{baseUrl}}/api/marks/1`

**Expected status:** `200 OK`

An unknown mark ID returns `404 Not Found`.

## 4. Update marks

**Method:** PUT
**URL:** `{{baseUrl}}/api/marks/1`

**Body:**

```json
{
  "studentId": 1,
  "subjectId": 1,
  "assessmentName": "Quiz 1",
  "assessmentType": "QUIZ",
  "marksObtained": 19,
  "maximumMarks": 20,
  "assessmentDate": "2026-01-15",
  "remarks": "Updated after review"
}
```

**Expected status:** `200 OK`

The response recalculates the percentage and grade. For 19 out of 20, the percentage is 95.0000 and the grade is `A+`.

## 5. Delete marks

**Method:** DELETE
**URL:** `{{baseUrl}}/api/marks/1`

**Expected status:** `204 No Content`

The response body is empty.

## 6. Get marks by student

**Method:** GET
**URL:** `{{baseUrl}}/api/marks/student/1`

**Expected status:** `200 OK`

This returns all assessments for the student. An unknown student ID returns `404 Not Found`.

## 7. Get marks by subject

**Method:** GET
**URL:** `{{baseUrl}}/api/marks/subject/1`

**Expected status:** `200 OK`

This returns all assessments for the subject. An unknown subject ID returns `404 Not Found`.

## 8. Get marks by student and subject

**Method:** GET
**URL:** `{{baseUrl}}/api/marks/student/1/subject/1`

**Expected status:** `200 OK`

This returns all assessments for the selected student in the selected subject.

## 9. Get student performance summary

**Method:** GET
**URL:** `{{baseUrl}}/api/marks/student/1/performance`

**Expected status:** `200 OK`

**Example response:**

```json
{
  "studentId": 1,
  "studentName": "Kamal Perera",
  "assessmentCount": 3,
  "totalMarksObtained": 75.0000,
  "totalMaximumMarks": 100.0000,
  "overallAveragePercentage": 75.0000,
  "overallGrade": "B",
  "subjectPerformance": [
	{
	  "subjectId": 1,
	  "subjectCode": "CS101",
	  "subjectName": "Programming Fundamentals",
	  "assessmentCount": 3,
	  "totalMarksObtained": 75.0000,
	  "totalMaximumMarks": 100.0000,
	  "averagePercentage": 75.0000,
	  "grade": "B"
	}
  ]
}
```

A student with no marks receives zero totals, an empty `subjectPerformance` array, and `overallGrade` equal to `N/A`.

## Percentage and grade calculation

Percentage is calculated as:

`marksObtained / maximumMarks * 100`

The overall student percentage is weighted using the total marks:

`sum(marksObtained) / sum(maximumMarks) * 100`

It is not calculated by averaging individual assessment percentages. This matters when assessments have different maximum marks.

The default grade scale is:

| Percentage | Grade |
|---:|:---|
| 90â€“100 | A+ |
| 80â€“89.99 | A |
| 70â€“79.99 | B |
| 60â€“69.99 | C |
| 50â€“59.99 | D |
| 0â€“49.99 | F |

Examples:

| Marks | Percentage | Grade |
|---|---:|:---|
| 95 / 100 | 95.0000 | A+ |
| 90 / 100 | 90.0000 | A+ |
| 89 / 100 | 89.0000 | A |
| 79 / 100 | 79.0000 | B |
| 69 / 100 | 69.0000 | C |
| 59 / 100 | 59.0000 | D |
| 49 / 100 | 49.0000 | F |
| 0 / 100 | 0.0000 | F |

## Error examples

### Student not found

**Status:** `404 Not Found`

```json
{
  "timestamp": "2026-01-15T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Student was not found",
  "path": "/api/marks"
}
```

### Student is not assigned to the subject

**Status:** `409 Conflict`

```json
{
  "timestamp": "2026-01-15T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Student must be assigned to the subject before marks can be recorded",
  "path": "/api/marks"
}
```

### Marks exceed maximum marks

**Status:** `400 Bad Request`

```json
{
  "timestamp": "2026-01-15T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Marks obtained must not exceed maximum marks",
  "path": "/api/marks"
}
```

### Duplicate assessment

**Status:** `409 Conflict`

A duplicate is identified by student, subject, assessment name, and assessment type.

```json
{
  "timestamp": "2026-01-15T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "An assessment with the same name and type already exists for this student and subject",
  "path": "/api/marks"
}
```

## Suggested Postman tests

For a successful POST request:

```javascript
pm.test("Status is 201 Created", function () {
	pm.response.to.have.status(201);
});

const data = pm.response.json();
pm.test("Percentage and grade are calculated", function () {
	pm.expect(data.percentage).to.exist;
	pm.expect(data.grade).to.be.oneOf(["A+", "A", "B", "C", "D", "F"]);
});
```

For the performance endpoint:

```javascript
pm.test("Performance summary returned", function () {
	pm.response.to.have.status(200);
	const data = pm.response.json();
	pm.expect(data).to.have.property("studentId");
	pm.expect(data.subjectPerformance).to.be.an("array");
});
```
