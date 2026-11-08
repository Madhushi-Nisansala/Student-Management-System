package com.SMS.student.management.system.marks;

import java.math.BigDecimal;
import java.util.List;

public record StudentPerformanceDTO(Long studentId, String studentName, long assessmentCount,
		BigDecimal totalMarksObtained, BigDecimal totalMaximumMarks, BigDecimal overallAveragePercentage,
		String overallGrade, List<SubjectPerformanceDTO> subjectPerformance) {
}