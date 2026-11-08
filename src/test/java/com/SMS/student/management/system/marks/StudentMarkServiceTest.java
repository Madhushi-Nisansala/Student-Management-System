package com.SMS.student.management.system.marks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.student_subject.StudentSubjectRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class StudentMarkServiceTest {
	private StudentMarkRepository repository;
	private StudentRepository studentRepository;
	private SubjectRepository subjectRepository;
	private StudentSubjectRepository assignmentRepository;
	private StudentMarkService service;
	private Student student;
	private Subject subject;

	@BeforeEach
	void setUp() {
		repository = Mockito.mock(StudentMarkRepository.class);
		studentRepository = Mockito.mock(StudentRepository.class);
		subjectRepository = Mockito.mock(SubjectRepository.class);
		assignmentRepository = Mockito.mock(StudentSubjectRepository.class);
		service = new StudentMarkService(repository, studentRepository, subjectRepository, assignmentRepository);
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
	void createsMarkAndCalculatesDerivedValues() {
		givenAssignedStudentAndSubject();
		when(repository.existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentType(1L, 2L, "Quiz 1", AssessmentType.QUIZ)).thenReturn(false);
		when(repository.save(any(StudentMark.class))).thenAnswer(invocation -> invocation.getArgument(0));

		StudentMarkResponseDTO response = service.createMark(request(BigDecimal.valueOf(18), BigDecimal.valueOf(20), "Quiz 1", AssessmentType.QUIZ));

		assertEquals(new BigDecimal("90.0000"), response.percentage());
		assertEquals("A+", response.grade());
	}

	@Test
	void rejectsMarksWhenStudentIsNotAssigned() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(assignmentRepository.existsByStudentIdAndSubjectId(1L, 2L)).thenReturn(false);

		MarksException exception = assertThrows(MarksException.class, () -> service.createMark(request(BigDecimal.TEN, BigDecimal.valueOf(20), "Quiz 1", AssessmentType.QUIZ)));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		verify(repository, never()).save(any(StudentMark.class));
	}

	@Test
	void rejectsMarksOutsideValidRange() {
		MarksException negative = assertThrows(MarksException.class, () -> service.calculatePercentage(BigDecimal.valueOf(-1), BigDecimal.TEN));
		assertEquals(HttpStatus.BAD_REQUEST, negative.getStatus());
		assertThrows(MarksException.class, () -> service.calculatePercentage(BigDecimal.valueOf(11), BigDecimal.TEN));
		assertThrows(MarksException.class, () -> service.calculatePercentage(BigDecimal.ONE, BigDecimal.ZERO));
	}

	@Test
	void rejectsDuplicateAssessment() {
		givenAssignedStudentAndSubject();
		when(repository.existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentType(1L, 2L, "Quiz 1", AssessmentType.QUIZ)).thenReturn(true);
		MarksException exception = assertThrows(MarksException.class, () -> service.createMark(request(BigDecimal.TEN, BigDecimal.valueOf(20), "Quiz 1", AssessmentType.QUIZ)));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
	}

	@Test
	void supportsCrudAndFilters() {
		StudentMark mark = mark(BigDecimal.TEN, BigDecimal.valueOf(20));
		when(repository.findAll()).thenReturn(List.of(mark));
		when(repository.findById(3L)).thenReturn(Optional.of(mark));
		when(repository.findByStudentId(1L)).thenReturn(List.of(mark));
		when(repository.findBySubjectId(2L)).thenReturn(List.of(mark));
		when(repository.findByStudentIdAndSubjectId(1L, 2L)).thenReturn(List.of(mark));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));

		assertEquals(1, service.getAllMarks().size());
		assertEquals(1, service.getMarksByStudentId(1L).size());
		assertEquals(1, service.getMarksBySubjectId(2L).size());
		assertEquals(1, service.getMarksByStudentAndSubject(1L, 2L).size());
		assertEquals(1L, service.getMarkById(3L).studentId());
		service.deleteMark(3L);
		verify(repository).delete(mark);
	}

	@Test
	void calculatesWeightedStudentPerformance() {
		StudentMark first = mark(BigDecimal.valueOf(8), BigDecimal.TEN);
		StudentMark second = mark(BigDecimal.valueOf(15), BigDecimal.valueOf(20));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(repository.findByStudentId(1L)).thenReturn(List.of(first, second));

		StudentPerformanceDTO summary = service.getStudentPerformance(1L);

		assertEquals(new BigDecimal("23.0000"), summary.totalMarksObtained());
		assertEquals(new BigDecimal("30.0000"), summary.totalMaximumMarks());
		assertEquals(new BigDecimal("76.6667"), summary.overallAveragePercentage());
		assertEquals("B", summary.overallGrade());
		assertEquals(1, summary.subjectPerformance().size());
	}

	@Test
	void handlesStudentWithoutMarks() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(repository.findByStudentId(1L)).thenReturn(List.of());

		StudentPerformanceDTO summary = service.getStudentPerformance(1L);

		assertEquals(BigDecimal.ZERO.setScale(4), summary.overallAveragePercentage());
		assertEquals("N/A", summary.overallGrade());
		assertEquals(0, summary.assessmentCount());
	}

	@Test
	void returnsNotFoundForUnknownMark() {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		MarksException exception = assertThrows(MarksException.class, () -> service.getMarkById(99L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	private void givenAssignedStudentAndSubject() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(assignmentRepository.existsByStudentIdAndSubjectId(1L, 2L)).thenReturn(true);
	}

	private StudentMarkRequestDTO request(BigDecimal obtained, BigDecimal maximum, String name, AssessmentType type) {
		return new StudentMarkRequestDTO(1L, 2L, name, type, obtained, maximum, LocalDate.now(), null);
	}

	private StudentMark mark(BigDecimal obtained, BigDecimal maximum) {
		StudentMark mark = Mockito.mock(StudentMark.class);
		when(mark.getStudent()).thenReturn(student);
		when(mark.getSubject()).thenReturn(subject);
		when(mark.getMarksObtained()).thenReturn(obtained);
		when(mark.getMaximumMarks()).thenReturn(maximum);
		when(mark.getPercentage()).thenReturn(GradeCalculator.percentage(obtained, maximum));
		when(mark.getGrade()).thenReturn(GradeCalculator.grade(GradeCalculator.percentage(obtained, maximum)));
		when(mark.getAssessmentName()).thenReturn("Quiz");
		when(mark.getAssessmentType()).thenReturn(AssessmentType.QUIZ);
		when(mark.getAssessmentDate()).thenReturn(LocalDate.now());
		return mark;
	}
}
