package com.SMS.student.management.system.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	boolean existsByStudentIdAndSubjectIdAndAttendanceDate(Long studentId, Long subjectId, LocalDate attendanceDate);
	boolean existsByStudentIdAndSubjectIdAndAttendanceDateAndIdNot(Long studentId, Long subjectId, LocalDate attendanceDate, Long id);
	List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
	List<Attendance> findBySubjectIdOrderByAttendanceDateDesc(Long subjectId);
	List<Attendance> findByAttendanceDateOrderByStudentId(LocalDate date);
	List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate startDate, LocalDate endDate);
	Page<Attendance> findAllByOrderByAttendanceDateDesc(Pageable pageable);
	long countByStatus(AttendanceStatus status);
	long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);
	long countBySubjectIdAndStatus(Long subjectId, AttendanceStatus status);
	long countByStudentId(Long studentId);
	long countBySubjectId(Long subjectId);
	Optional<Attendance> findById(Long id);
}
