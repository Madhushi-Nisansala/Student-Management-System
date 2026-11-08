package com.SMS.student.management.system.marks;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentMarkRequestDTO(
		@NotNull Long studentId,
		@NotNull Long subjectId,
		@NotBlank @Size(max = 120) String assessmentName,
		@NotNull AssessmentType assessmentType,
		@NotNull @DecimalMin(value = "0.0") BigDecimal marksObtained,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal maximumMarks,
		@NotNull @PastOrPresent LocalDate assessmentDate,
		@Size(max = 500) String remarks) {
}