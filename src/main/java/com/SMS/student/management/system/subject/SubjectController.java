package com.SMS.student.management.system.subject;

import java.util.List;
import jakarta.validation.Valid;
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
@RequestMapping("/api/subjects")
public class SubjectController {
	private final SubjectService service;

	public SubjectController(SubjectService service) { this.service = service; }

	@GetMapping
	public List<SubjectResponse> getAll() { return service.getAll(); }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SubjectResponse create(@Valid @RequestBody SubjectRequest request) { return service.create(request); }

	@GetMapping("/{id}")
	public SubjectResponse getById(@PathVariable Long id) { return service.getById(id); }

	@PutMapping("/{id}")
	public SubjectResponse update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) { return service.update(id, request); }

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { service.delete(id); }
}
