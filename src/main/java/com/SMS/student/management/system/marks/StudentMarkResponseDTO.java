package com.SMS.student.management.system.marks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentMarkResponseDTO(Long id, Long studentId, String studentName, Long subjectId,
		String subjectCode, String subjectName, String assessmentName, AssessmentType assessmentType,
		BigDecimal marksObtained, BigDecimal maximumMarks, BigDecimal percentage, String grade,
		LocalDate assessmentDate, String remarks, LocalDateTime createdAt, LocalDateTime updatedAt) {
}