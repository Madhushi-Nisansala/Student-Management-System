package com.SMS.student.management.system.attendance;

import java.math.BigDecimal;

public record AttendanceSummaryDTO(Long studentId, Long subjectId, long totalDays, long presentDays,
								   long absentDays, long lateDays, BigDecimal attendancePercentage) {
}
