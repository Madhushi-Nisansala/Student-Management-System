package com.SMS.student.management.system.subject;

import java.time.LocalDateTime;

public record SubjectResponse(Long id, String subjectCode, String subjectName, String description,
		Integer credits, LocalDateTime createdAt) {
}