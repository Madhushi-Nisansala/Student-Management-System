package com.SMS.student.management.system.common;

import com.SMS.student.management.system.attendance.AttendanceException;
import com.SMS.student.management.system.student.StudentConflictException;
import com.SMS.student.management.system.student_subject.StudentSubjectException;
import com.SMS.student.management.system.subject.SubjectException;
import com.SMS.student.management.system.marks.MarksException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(AttendanceException.class)
	public ResponseEntity<ApiError> attendance(AttendanceException ex, HttpServletRequest request) { return error(ex.getStatus(), ex.getMessage(), request); }

	@ExceptionHandler(StudentConflictException.class)
	public ResponseEntity<ApiError> studentConflict(StudentConflictException ex, HttpServletRequest request) { return error(HttpStatus.CONFLICT, ex.getMessage(), request); }

	@ExceptionHandler(SubjectException.class)
	public ResponseEntity<ApiError> subject(SubjectException ex, HttpServletRequest request) { return error(ex.getStatus(), ex.getMessage(), request); }

	@ExceptionHandler(StudentSubjectException.class)
	public ResponseEntity<ApiError> studentSubject(StudentSubjectException ex, HttpServletRequest request) { return error(ex.getStatus(), ex.getMessage(), request); }

	@ExceptionHandler(MarksException.class)
	public ResponseEntity<ApiError> marks(MarksException ex, HttpServletRequest request) { return error(ex.getStatus(), ex.getMessage(), request); }

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest request) { return error(HttpStatus.BAD_REQUEST, "Invalid request parameter, date, status, or JSON body", request); }

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> conflict(HttpServletRequest request) { return error(HttpStatus.CONFLICT, "Resource conflicts with an existing record", request); }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> unexpected(HttpServletRequest request) { return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request); }

	private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
	}
}
