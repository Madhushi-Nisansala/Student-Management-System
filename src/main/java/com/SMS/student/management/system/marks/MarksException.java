package com.SMS.student.management.system.marks;

import org.springframework.http.HttpStatus;

public class MarksException extends RuntimeException {
	private final HttpStatus status;

	public MarksException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() { return status; }
}