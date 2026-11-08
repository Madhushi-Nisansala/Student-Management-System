package com.SMS.student.management.system.attendance;

import org.springframework.http.HttpStatus;

public class AttendanceException extends RuntimeException {
	private final HttpStatus status;

	public AttendanceException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() { return status; }
}
