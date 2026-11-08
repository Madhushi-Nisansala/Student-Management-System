package com.SMS.student.management.system.student_subject;

import java.time.LocalDateTime;

public record StudentSubjectResponse(Long id, Long studentId, String studentName, Long subjectId,
		String subjectCode, String subjectName, LocalDateTime assignedAt) {
}