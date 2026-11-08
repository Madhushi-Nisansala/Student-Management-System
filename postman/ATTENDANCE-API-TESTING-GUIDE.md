# Attendance API Testing Guide

Import Student-Management-Attendance-Collection.json and Student-Management-Local-Environment.json into Postman. Select the local environment, replace studentId and subjectId with existing records, and run Attendance Management in order. The collection covers create (201), reads (200), update (200), delete (204), summaries, filtering, duplicate (409), invalid data (400), missing resources (404), invalid ranges, future dates, and oversized remarks.

PowerShell:
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd clean verify

The application uses DB_URL, DB_USERNAME, and DB_PASSWORD for MySQL. Test execution uses isolated H2. Future dates are prohibited; the collection changes a future default date to today before valid creation. After deletion, GET the saved attendanceId and expect 404. Newman: newman run .\postman\Student-Management-Attendance-Collection.json -e .\postman\Student-Management-Local-Environment.json
