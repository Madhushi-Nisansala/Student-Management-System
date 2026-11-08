package com.SMS.student.management.system.student_subject;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentSubjectService {
	private final StudentSubjectRepository repository;
	private final StudentRepository studentRepository;
	private final SubjectRepository subjectRepository;

	public StudentSubjectService(StudentSubjectRepository repository, StudentRepository studentRepository,
			SubjectRepository subjectRepository) {
		this.repository = repository;
		this.studentRepository = studentRepository;
		this.subjectRepository = subjectRepository;
	}

	public StudentSubjectResponse assign(Long studentId, Long subjectId) {
		Student student = getStudent(studentId);
		Subject subject = getSubject(subjectId);
		if (repository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
			throw conflict("Student is already assigned to this subject");
		}
		try {
			return toResponse(repository.save(new StudentSubject(student, subject)));
		} catch (DataIntegrityViolationException ex) {
			throw conflict("Student is already assigned to this subject");
		}
	}

	@Transactional(readOnly = true)
	public List<StudentSubjectResponse> subjectsForStudent(Long studentId) {
		getStudent(studentId);
		return repository.findByStudentId(studentId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<StudentSubjectResponse> studentsForSubject(Long subjectId) {
		getSubject(subjectId);
		return repository.findBySubjectId(subjectId).stream().map(this::toResponse).toList();
	}

	public void remove(Long studentId, Long subjectId) {
		getStudent(studentId);
		getSubject(subjectId);
		StudentSubject assignment = repository.findByStudentIdAndSubjectId(studentId, subjectId)
				.orElseThrow(() -> new StudentSubjectException("Student-subject assignment was not found", HttpStatus.NOT_FOUND));
		repository.delete(assignment);
	}

	private Student getStudent(Long id) {
		return studentRepository.findById(id).orElseThrow(() -> new StudentSubjectException("Student was not found", HttpStatus.NOT_FOUND));
	}

	private Subject getSubject(Long id) {
		return subjectRepository.findById(id).orElseThrow(() -> new StudentSubjectException("Subject was not found", HttpStatus.NOT_FOUND));
	}

	private StudentSubjectResponse toResponse(StudentSubject assignment) {
		Student student = assignment.getStudent();
		Subject subject = assignment.getSubject();
		return new StudentSubjectResponse(assignment.getId(), student.getId(), student.getFirstName() + " " + student.getLastName(),
				subject.getId(), subject.getSubjectCode(), subject.getSubjectName(), assignment.getAssignedAt());
	}

	private StudentSubjectException conflict(String message) { return new StudentSubjectException(message, HttpStatus.CONFLICT); }
}