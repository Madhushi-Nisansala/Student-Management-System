package com.SMS.student.management.system.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponseDTO(Long id, Long studentId, String studentName, Long subjectId, String subjectName,
									LocalDate attendanceDate, AttendanceStatus status, String remarks,
									LocalDateTime createdAt, LocalDateTime updatedAt) {
}
