package com.SMS.student.management.system.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

class AttendanceServiceTest {
	private AttendanceRepository attendanceRepository;
	private StudentRepository studentRepository;
	private SubjectRepository subjectRepository;
	private AttendanceService service;

	@BeforeEach
	void setUp() {
		attendanceRepository = Mockito.mock(AttendanceRepository.class);
		studentRepository = Mockito.mock(StudentRepository.class);
		subjectRepository = Mockito.mock(SubjectRepository.class);
		service = new AttendanceService(attendanceRepository, studentRepository, subjectRepository);
	}

	@Test
	void rejectsDuplicateAttendance() {
		Student student = mockStudent(1L);
		Subject subject = mockSubject(2L);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDate(1L, 2L, LocalDate.now())).thenReturn(true);
		AttendanceException exception = assertThrows(AttendanceException.class, () -> service.create(new AttendanceRequestDTO(1L, 2L, LocalDate.now(), AttendanceStatus.PRESENT, null)));
		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
	}

	@Test
	void rejectsFutureDate() {
		AttendanceException exception = assertThrows(AttendanceException.class, () -> service.create(new AttendanceRequestDTO(1L, 2L, LocalDate.now().plusDays(1), AttendanceStatus.PRESENT, null)));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void calculatesSummaryWithLateAsAttended() {
		Student student = mockStudent(1L);
		Subject subject = mockSubject(2L);
		Attendance present = new Attendance(student, subject, LocalDate.now().minusDays(2), AttendanceStatus.PRESENT, null);
		Attendance late = new Attendance(student, subject, LocalDate.now().minusDays(1), AttendanceStatus.LATE, null);
		Attendance absent = new Attendance(student, subject, LocalDate.now(), AttendanceStatus.ABSENT, null);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(attendanceRepository.findAll()).thenReturn(List.of(present, late, absent));
		AttendanceSummaryDTO summary = service.studentSummary(1L);
		assertEquals(3, summary.totalDays());
		assertEquals(66.67, summary.attendancePercentage().doubleValue());
	}

	@Test
	void rejectsUnknownStudent() {
		when(studentRepository.findById(99L)).thenReturn(Optional.empty());
		AttendanceException exception = assertThrows(AttendanceException.class, () -> service.create(new AttendanceRequestDTO(99L, 2L, LocalDate.now(), AttendanceStatus.PRESENT, null)));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	@Test
	void rejectsUnknownSubject() {
		Student student = mockStudent(1L);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(99L)).thenReturn(Optional.empty());
		AttendanceException exception = assertThrows(AttendanceException.class, () -> service.create(new AttendanceRequestDTO(1L, 99L, LocalDate.now(), AttendanceStatus.PRESENT, null)));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	@Test
	void rejectsInvalidDateRange() {
		AttendanceException exception = assertThrows(AttendanceException.class, () -> service.byRange(LocalDate.now(), LocalDate.now().minusDays(1)));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void supportsStudentSubjectAndDateFilters() {
		Student student = mockStudent(1L);
		Subject subject = mockSubject(2L);
		Attendance attendance = new Attendance(student, subject, LocalDate.now(), AttendanceStatus.PRESENT, null);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(1L)).thenReturn(List.of(attendance));
		when(attendanceRepository.findBySubjectIdOrderByAttendanceDateDesc(2L)).thenReturn(List.of(attendance));
		when(attendanceRepository.findByAttendanceDateOrderByStudentId(LocalDate.now())).thenReturn(List.of(attendance));
		assertEquals(1, service.byStudent(1L).size());
		assertEquals(1, service.bySubject(2L).size());
		assertEquals(1, service.byDate(LocalDate.now()).size());
	}

	@Test
	void updatesAndDeletesExistingAttendance() {
		Student student = mockStudent(1L);
		Subject subject = mockSubject(2L);
		Attendance attendance = new Attendance(student, subject, LocalDate.now(), AttendanceStatus.PRESENT, "before");
		when(attendanceRepository.findById(7L)).thenReturn(Optional.of(attendance));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(attendanceRepository.save(attendance)).thenReturn(attendance);
		AttendanceResponseDTO updated = service.update(7L, new AttendanceRequestDTO(1L, 2L, LocalDate.now(), AttendanceStatus.LATE, "after"));
		assertEquals(AttendanceStatus.LATE, updated.status());
		service.delete(7L);
	}

	private Student mockStudent(Long id) {
		Student student = mock(Student.class);
		doReturn(id).when(student).getId();
		doReturn("Test").when(student).getFirstName();
		doReturn("Student").when(student).getLastName();
		return student;
	}

	private Subject mockSubject(Long id) {
		Subject subject = mock(Subject.class);
		doReturn(id).when(subject).getId();
		doReturn("Mathematics").when(subject).getName();
		return subject;
	}
}
