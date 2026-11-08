package com.SMS.student.management.system.student_subject;

import org.springframework.http.HttpStatus;

public class StudentSubjectException extends RuntimeException {
	private final HttpStatus status;

	public StudentSubjectException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() { return status; }
}