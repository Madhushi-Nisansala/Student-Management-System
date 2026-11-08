package com.SMS.student.management.system.student;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/students")
public class StudentController {
	private final StudentRepository repository;
	public StudentController(StudentRepository repository) { this.repository = repository; }
	@GetMapping
	public List<Student> getAll() { return repository.findAll(); }

	@GetMapping("/{id}")
	public ResponseEntity<Student> getById(@PathVariable Long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
		if (student.getEmail() != null && repository.existsByEmail(student.getEmail())) {
			throw new StudentConflictException("Email is already registered");
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(student));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Student> update(@PathVariable Long id, @Valid @RequestBody Student request) {
		return repository.findById(id).map(student -> {
			if (request.getEmail() != null && repository.existsByEmailAndIdNot(request.getEmail(), id)) {
				throw new StudentConflictException("Email is already registered");
			}
			student.setFirstName(request.getFirstName());
			student.setLastName(request.getLastName());
			student.setEmail(request.getEmail());
			return ResponseEntity.ok(repository.save(student));
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (repository.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		repository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
