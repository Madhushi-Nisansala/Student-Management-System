package com.SMS.student.management.system.subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.SMS.student.management.system.student_subject.StudentSubjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class SubjectServiceTest {
	private SubjectRepository repository;
	private StudentSubjectRepository assignmentRepository;
	private SubjectService service;

	@BeforeEach
	void setUp() {
		repository = Mockito.mock(SubjectRepository.class);
		assignmentRepository = Mockito.mock(StudentSubjectRepository.class);
		service = new SubjectService(repository, assignmentRepository);
	}

	@Test
	void createsSubject() {
		when(repository.existsBySubjectCode("CS101")).thenReturn(false);
		Subject subject = new Subject("CS101", "Programming Fundamentals", "Introduction", 3);
		when(repository.save(any(Subject.class))).thenReturn(subject);

		SubjectResponse response = service.create(new SubjectRequest("CS101", "Programming Fundamentals", "Introduction", 3));

		assertEquals("CS101", response.subjectCode());
		assertEquals(3, response.credits());
	}

	@Test
	void rejectsDuplicateSubjectCode() {
		when(repository.existsBySubjectCode("CS101")).thenReturn(true);
		SubjectException exception = assertThrows(SubjectException.class,
				() -> service.create(new SubjectRequest("CS101", "Programming", null, 3)));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
	}

	@Test
	void updatesSubject() {
		Subject subject = new Subject("CS101", "Programming", null, 3);
		when(repository.findById(1L)).thenReturn(Optional.of(subject));
		when(repository.existsBySubjectCodeAndIdNot("CS101", 1L)).thenReturn(false);
		when(repository.save(subject)).thenReturn(subject);

		SubjectResponse response = service.update(1L, new SubjectRequest("CS101", "Advanced Programming", "Updated", 4));

		assertEquals("Advanced Programming", response.subjectName());
		assertEquals(4, response.credits());
	}

	@Test
	void deletesUnassignedSubject() {
		Subject subject = new Subject("CS101", "Programming", null, 3);
		when(repository.findById(1L)).thenReturn(Optional.of(subject));
		when(assignmentRepository.existsBySubjectId(null)).thenReturn(false);

		service.delete(1L);

		verify(repository).delete(subject);
	}

	@Test
	void preventsDeletingAssignedSubject() {
		Subject subject = new Subject("CS101", "Programming", null, 3);
		when(repository.findById(1L)).thenReturn(Optional.of(subject));
		when(assignmentRepository.existsBySubjectId(null)).thenReturn(true);

		SubjectException exception = assertThrows(SubjectException.class, () -> service.delete(1L));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		verify(repository, never()).delete(any(Subject.class));
	}

	@Test
	void returnsNotFoundForUnknownSubject() {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		SubjectException exception = assertThrows(SubjectException.class, () -> service.getById(99L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	@Test
	void listsSubjects() {
		when(repository.findAll()).thenReturn(List.of(new Subject("CS101", "Programming", null, 3)));
		assertEquals(1, service.getAll().size());
	}
}
