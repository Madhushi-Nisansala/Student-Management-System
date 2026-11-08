# Student API Postman Testing Guide

## Setup

1. Start the Spring Boot application.
2. Create a Postman environment with:
   - `baseUrl`: `http://localhost:8080`
   - `studentId`: `1` (replace this with the ID returned by POST)
3. Set `Content-Type: application/json` on POST and PUT requests.

## 1. Register Student

**Method:** POST
**URL:** `{{baseUrl}}/api/students`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "firstName": "Kamal",
  "lastName": "Perera",
  "email": "kamal.perera@example.com"
}
```

**Expected:** `201 Created`

```json
{
  "id": 1,
  "firstName": "Kamal",
  "lastName": "Perera",
  "email": "kamal.perera@example.com"
}
```

**Tests tab:**

```javascript
pm.test("Student was created", function () {
	pm.response.to.have.status(201);
});
pm.environment.set("studentId", pm.response.json().id);
```

## 2. Get All Students

**Method:** GET
**URL:** `{{baseUrl}}/api/students`

**Headers:** None required.

**Expected:** `200 OK`

```json
[
  {
	"id": 1,
	"firstName": "Kamal",
	"lastName": "Perera",
	"email": "kamal.perera@example.com"
  }
]
```

For an empty database, the response is `[]`.

**Tests tab:**

```javascript
pm.test("Students were returned", function () {
	pm.response.to.have.status(200);
	pm.expect(pm.response.json()).to.be.an("array");
});
```

## 3. Get Student by ID

**Method:** GET
**URL:** `{{baseUrl}}/api/students/{{studentId}}`

**Headers:** None required.

**Expected:** `200 OK`, with the student JSON shown above.

For an unknown ID, expected status is `404 Not Found` with an empty response body.

## 4. Update Student

**Method:** PUT
**URL:** `{{baseUrl}}/api/students/{{studentId}}`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "firstName": "Kamal",
  "lastName": "Silva",
  "email": "kamal.silva@example.com"
}
```

**Expected:** `200 OK`

```json
{
  "id": 1,
  "firstName": "Kamal",
  "lastName": "Silva",
  "email": "kamal.silva@example.com"
}
```

An unknown ID returns `404 Not Found`. An email already used by another student returns `409 Conflict`.

## 5. Delete Student

**Method:** DELETE
**URL:** `{{baseUrl}}/api/students/{{studentId}}`

**Headers:** None required.

**Expected:** `204 No Content` with an empty response body.

An unknown ID returns `404 Not Found`.

**Tests tab:**

```javascript
pm.test("Student was deleted", function () {
	pm.response.to.have.status(204);
	pm.expect(pm.response.text()).to.eql("");
});
```

## Validation and conflict examples

POST or PUT with a blank first name, blank last name, or invalid email returns `400 Bad Request`:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "firstName: must not be blank",
  "path": "/api/students"
}
```

Registering an email already in use returns `409 Conflict`:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email is already registered",
  "path": "/api/students"
}
```

Unexpected failures return `500 Internal Server Error` with a generic message; database credentials and internal exception details are not included.
