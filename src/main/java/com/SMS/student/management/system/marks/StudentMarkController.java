package com.SMS.student.management.system.marks;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marks")
public class StudentMarkController {
	private final StudentMarkService service;

	public StudentMarkController(StudentMarkService service) { this.service = service; }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public StudentMarkResponseDTO create(@Valid @RequestBody StudentMarkRequestDTO request) {
		return service.createMark(request);
	}

	@GetMapping
	public List<StudentMarkResponseDTO> getAll() { return service.getAllMarks(); }

	@GetMapping("/{id}")
	public StudentMarkResponseDTO getById(@PathVariable Long id) { return service.getMarkById(id); }

	@PutMapping("/{id}")
	public StudentMarkResponseDTO update(@PathVariable Long id, @Valid @RequestBody StudentMarkRequestDTO request) {
		return service.updateMark(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { service.deleteMark(id); }

	@GetMapping("/student/{studentId}")
	public List<StudentMarkResponseDTO> byStudent(@PathVariable Long studentId) {
		return service.getMarksByStudentId(studentId);
	}

	@GetMapping("/subject/{subjectId}")
	public List<StudentMarkResponseDTO> bySubject(@PathVariable Long subjectId) {
		return service.getMarksBySubjectId(subjectId);
	}

	@GetMapping("/student/{studentId}/subject/{subjectId}")
	public List<StudentMarkResponseDTO> byStudentAndSubject(@PathVariable Long studentId, @PathVariable Long subjectId) {
		return service.getMarksByStudentAndSubject(studentId, subjectId);
	}

	@GetMapping("/student/{studentId}/performance")
	public StudentPerformanceDTO performance(@PathVariable Long studentId) {
		return service.getStudentPerformance(studentId);
	}
}