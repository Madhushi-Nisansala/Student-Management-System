package com.SMS.student.management.system.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AttendanceRequestDTO(
		@NotNull(message = "studentId is required") Long studentId,
		@NotNull(message = "subjectId is required") Long subjectId,
		@NotNull(message = "attendanceDate is required") @PastOrPresent(message = "attendanceDate cannot be in the future") LocalDate attendanceDate,
		@NotNull(message = "status is required") AttendanceStatus status,
		@Size(max = 500, message = "remarks cannot exceed 500 characters") String remarks) {
}
