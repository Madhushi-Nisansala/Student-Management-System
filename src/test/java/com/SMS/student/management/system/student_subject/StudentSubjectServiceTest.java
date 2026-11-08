package com.SMS.student.management.system.student_subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class StudentSubjectServiceTest {
	private StudentSubjectRepository repository;
	private StudentRepository studentRepository;
	private SubjectRepository subjectRepository;
	private StudentSubjectService service;
	private Student student;
	private Subject subject;

	@BeforeEach
	void setUp() {
		repository = Mockito.mock(StudentSubjectRepository.class);
		studentRepository = Mockito.mock(StudentRepository.class);
		subjectRepository = Mockito.mock(SubjectRepository.class);
		service = new StudentSubjectService(repository, studentRepository, subjectRepository);
		student = Mockito.mock(Student.class);
		subject = Mockito.mock(Subject.class);
		doReturn(1L).when(student).getId();
		doReturn("Kamal").when(student).getFirstName();
		doReturn("Perera").when(student).getLastName();
		doReturn(2L).when(subject).getId();
		doReturn("CS101").when(subject).getSubjectCode();
		doReturn("Programming").when(subject).getSubjectName();
	}

	@Test
	void assignsSubjectToStudent() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(repository.existsByStudentIdAndSubjectId(1L, 2L)).thenReturn(false);
		when(repository.save(Mockito.any(StudentSubject.class))).thenAnswer(invocation -> invocation.getArgument(0));

		StudentSubjectResponse response = service.assign(1L, 2L);

		assertEquals(1L, response.studentId());
		assertEquals(2L, response.subjectId());
		assertEquals("CS101", response.subjectCode());
	}

	@Test
	void rejectsDuplicateAssignment() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(repository.existsByStudentIdAndSubjectId(1L, 2L)).thenReturn(true);

		StudentSubjectException exception = assertThrows(StudentSubjectException.class, () -> service.assign(1L, 2L));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
	}

	@Test
	void rejectsUnknownStudent() {
		when(studentRepository.findById(99L)).thenReturn(Optional.empty());
		StudentSubjectException exception = assertThrows(StudentSubjectException.class, () -> service.assign(99L, 2L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
		verify(subjectRepository, never()).findById(2L);
	}

	@Test
	void rejectsUnknownSubject() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(99L)).thenReturn(Optional.empty());
		StudentSubjectException exception = assertThrows(StudentSubjectException.class, () -> service.assign(1L, 99L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	@Test
	void listsAssignmentsForStudentAndSubject() {
		StudentSubject assignment = Mockito.mock(StudentSubject.class);
		when(assignment.getStudent()).thenReturn(student);
		when(assignment.getSubject()).thenReturn(subject);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(repository.findByStudentId(1L)).thenReturn(List.of(assignment));
		when(repository.findBySubjectId(2L)).thenReturn(List.of(assignment));

		assertEquals(1, service.subjectsForStudent(1L).size());
		assertEquals(1, service.studentsForSubject(2L).size());
	}

	@Test
	void removesOnlyTheAssignment() {
		StudentSubject assignment = Mockito.mock(StudentSubject.class);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(repository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(Optional.of(assignment));

		service.remove(1L, 2L);

		verify(repository).delete(assignment);
		verify(studentRepository, never()).deleteById(1L);
		verify(subjectRepository, never()).deleteById(2L);
	}
}
