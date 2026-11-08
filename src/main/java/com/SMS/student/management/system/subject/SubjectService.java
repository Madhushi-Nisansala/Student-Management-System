package com.SMS.student.management.system.subject;

import com.SMS.student.management.system.student_subject.StudentSubjectRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubjectService {
	private final SubjectRepository repository;
	private final StudentSubjectRepository assignmentRepository;

	public SubjectService(SubjectRepository repository, StudentSubjectRepository assignmentRepository) {
		this.repository = repository;
		this.assignmentRepository = assignmentRepository;
	}

	public SubjectResponse create(SubjectRequest request) {
		if (repository.existsBySubjectCode(request.subjectCode())) {
			throw conflict("Subject code is already registered");
		}
		try {
			return toResponse(repository.save(new Subject(request.subjectCode(), request.subjectName(), request.description(), request.credits())));
		} catch (DataIntegrityViolationException ex) {
			throw conflict("Subject code is already registered");
		}
	}

	@Transactional(readOnly = true)
	public List<SubjectResponse> getAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

	@Transactional(readOnly = true)
	public SubjectResponse getById(Long id) { return toResponse(getEntity(id)); }

	public SubjectResponse update(Long id, SubjectRequest request) {
		Subject subject = getEntity(id);
		if (repository.existsBySubjectCodeAndIdNot(request.subjectCode(), id)) {
			throw conflict("Subject code is already registered");
		}
		subject.setSubjectCode(request.subjectCode());
		subject.setSubjectName(request.subjectName());
		subject.setDescription(request.description());
		subject.setCredits(request.credits());
		return toResponse(repository.save(subject));
	}

	public void delete(Long id) {
		Subject subject = getEntity(id);
		if (assignmentRepository.existsBySubjectId(subject.getId())) {
			throw conflict("Subject cannot be deleted while it is assigned to students");
		}
		repository.delete(subject);
	}

	private Subject getEntity(Long id) {
		return repository.findById(id).orElseThrow(() -> new SubjectException("Subject was not found", HttpStatus.NOT_FOUND));
	}

	private SubjectResponse toResponse(Subject subject) {
		return new SubjectResponse(subject.getId(), subject.getSubjectCode(), subject.getSubjectName(), subject.getDescription(), subject.getCredits(), subject.getCreatedAt());
	}

	private SubjectException conflict(String message) { return new SubjectException(message, HttpStatus.CONFLICT); }
}