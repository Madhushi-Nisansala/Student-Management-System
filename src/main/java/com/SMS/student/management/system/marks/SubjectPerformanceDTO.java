package com.SMS.student.management.system.marks;

import java.math.BigDecimal;

public record SubjectPerformanceDTO(Long subjectId, String subjectCode, String subjectName,
		long assessmentCount, BigDecimal totalMarksObtained, BigDecimal totalMaximumMarks,
		BigDecimal averagePercentage, String grade) {
}