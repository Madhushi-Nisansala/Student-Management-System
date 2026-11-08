package com.SMS.student.management.system.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
		@NotBlank @Size(max = 30) String subjectCode,
		@NotBlank @Size(max = 120) String subjectName,
		@Size(max = 1000) String description,
		@NotNull @Positive Integer credits) {
}