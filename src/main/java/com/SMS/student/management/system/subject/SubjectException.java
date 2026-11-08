package com.SMS.student.management.system.subject;

import org.springframework.http.HttpStatus;

public class SubjectException extends RuntimeException {
	private final HttpStatus status;

	public SubjectException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() { return status; }
}