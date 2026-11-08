package com.SMS.student.management.system.attendance;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
	private final AttendanceService service;

	public AttendanceController(AttendanceService service) { this.service = service; }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AttendanceResponseDTO create(@Valid @RequestBody AttendanceRequestDTO request) { return service.create(request); }

	@GetMapping
	public Page<AttendanceResponseDTO> getAll(@RequestParam(required = false) Long studentId, @RequestParam(required = false) Long subjectId,
											   @RequestParam(required = false) LocalDate date, @RequestParam(required = false) LocalDate startDate,
											   @RequestParam(required = false) LocalDate endDate, @RequestParam(required = false) AttendanceStatus status,
											   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
											   @RequestParam(defaultValue = "attendanceDate,desc") String sort) {
		String[] parts = sort.split(",", 2);
		Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(direction, parts[0]));
		return service.search(studentId, subjectId, date, startDate, endDate, status, pageable);
	}

	@GetMapping("/{id}")
	public AttendanceResponseDTO getById(@PathVariable Long id) { return service.getById(id); }

	@PutMapping("/{id}")
	public AttendanceResponseDTO update(@PathVariable Long id, @Valid @RequestBody AttendanceRequestDTO request) { return service.update(id, request); }

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) { service.delete(id); }

	@GetMapping("/student/{studentId}")
	public List<AttendanceResponseDTO> byStudent(@PathVariable Long studentId) { return service.byStudent(studentId); }
	@GetMapping("/subject/{subjectId}")
	public List<AttendanceResponseDTO> bySubject(@PathVariable Long subjectId) { return service.bySubject(subjectId); }
	@GetMapping("/date/{date}")
	public List<AttendanceResponseDTO> byDate(@PathVariable LocalDate date) { return service.byDate(date); }
	@GetMapping("/range")
	public List<AttendanceResponseDTO> byRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) { return service.byRange(startDate, endDate); }
	@GetMapping("/summary/student/{studentId}")
	public AttendanceSummaryDTO studentSummary(@PathVariable Long studentId) { return service.studentSummary(studentId); }
	@GetMapping("/summary/subject/{subjectId}")
	public AttendanceSummaryDTO subjectSummary(@PathVariable Long subjectId) { return service.subjectSummary(subjectId); }
}
