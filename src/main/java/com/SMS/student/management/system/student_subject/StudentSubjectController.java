package com.SMS.student.management.system.student_subject;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentSubjectController {
	private final StudentSubjectService service;

	public StudentSubjectController(StudentSubjectService service) { this.service = service; }

	@PostMapping("/api/students/{studentId}/subjects/{subjectId}")
	@ResponseStatus(HttpStatus.CREATED)
	public StudentSubjectResponse assign(@PathVariable Long studentId, @PathVariable Long subjectId) {
		return service.assign(studentId, subjectId);
	}

	@GetMapping("/api/students/{studentId}/subjects")
	public List<StudentSubjectResponse> subjectsForStudent(@PathVariable Long studentId) {
		return service.subjectsForStudent(studentId);
	}

	@GetMapping("/api/subjects/{subjectId}/students")
	public List<StudentSubjectResponse> studentsForSubject(@PathVariable Long subjectId) {
		return service.studentsForSubject(subjectId);
	}

	@DeleteMapping("/api/students/{studentId}/subjects/{subjectId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable Long studentId, @PathVariable Long subjectId) {
		service.remove(studentId, subjectId);
	}
}