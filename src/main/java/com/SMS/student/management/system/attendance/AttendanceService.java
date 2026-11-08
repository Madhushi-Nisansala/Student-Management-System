package com.SMS.student.management.system.attendance;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {
	private final AttendanceRepository attendanceRepository;
	private final StudentRepository studentRepository;
	private final SubjectRepository subjectRepository;

	public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository,
							 SubjectRepository subjectRepository) {
		this.attendanceRepository = attendanceRepository;
		this.studentRepository = studentRepository;
		this.subjectRepository = subjectRepository;
	}

	public AttendanceResponseDTO create(AttendanceRequestDTO request) {
		validateDate(request.attendanceDate());
		Student student = getStudent(request.studentId());
		Subject subject = getSubject(request.subjectId());
		ensureUnique(request, null);
		try {
			return toResponse(attendanceRepository.save(new Attendance(student, subject, request.attendanceDate(), request.status(), request.remarks())));
		} catch (org.springframework.dao.DataIntegrityViolationException ex) {
			throw new AttendanceException("Attendance already exists for this student, subject and date", HttpStatus.CONFLICT);
		}
	}

	public AttendanceResponseDTO update(Long id, AttendanceRequestDTO request) {
		validateDate(request.attendanceDate());
		Attendance attendance = getEntity(id);
		Student student = getStudent(request.studentId());
		Subject subject = getSubject(request.subjectId());
		if (attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDateAndIdNot(request.studentId(), request.subjectId(), request.attendanceDate(), id)) {
			throw new AttendanceException("Attendance already exists for this student, subject and date", HttpStatus.CONFLICT);
		}
		attendance.update(request.attendanceDate(), request.status(), request.remarks());
		if (!attendance.getStudent().getId().equals(student.getId()) || !attendance.getSubject().getId().equals(subject.getId())) {
			attendance.updateRelationships(student, subject);
		}
		return toResponse(attendanceRepository.save(attendance));
	}

	@Transactional(readOnly = true)
	public AttendanceResponseDTO getById(Long id) { return toResponse(getEntity(id)); }

	@Transactional(readOnly = true)
	public Page<AttendanceResponseDTO> search(Long studentId, Long subjectId, LocalDate date, LocalDate startDate,
											   LocalDate endDate, AttendanceStatus status, Pageable pageable) {
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new AttendanceException("startDate must not be after endDate", HttpStatus.BAD_REQUEST);
		}
		Predicate<Attendance> filter = a -> (studentId == null || a.getStudent().getId().equals(studentId))
				&& (subjectId == null || a.getSubject().getId().equals(subjectId))
				&& (date == null || a.getAttendanceDate().equals(date))
				&& (startDate == null || !a.getAttendanceDate().isBefore(startDate))
				&& (endDate == null || !a.getAttendanceDate().isAfter(endDate))
				&& (status == null || a.getStatus() == status);
		List<Attendance> all = attendanceRepository.findAll().stream().filter(filter)
				.sorted(comparator(pageable)).toList();
		int from = Math.min((int) pageable.getOffset(), all.size());
		int to = Math.min(from + pageable.getPageSize(), all.size());
		return new PageImpl<>(all.subList(from, to).stream().map(this::toResponse).toList(), pageable, all.size());
	}

	private Comparator<Attendance> comparator(Pageable pageable) {
		Comparator<Attendance> comparator = Comparator.comparing(Attendance::getAttendanceDate).reversed();
		if (pageable.getSort().isSorted()) {
			var order = pageable.getSort().iterator().next();
			comparator = Comparator.comparing(a -> sortValue(a, order.getProperty()), Comparator.nullsLast(Comparator.naturalOrder()));
			if (order.isDescending()) comparator = comparator.reversed();
		}
		return comparator;
	}

	@SuppressWarnings("unchecked")
	private Comparable<Object> sortValue(Attendance a, String property) {
		Object value = switch (property) {
			case "id" -> a.getId(); case "attendanceDate" -> a.getAttendanceDate(); case "status" -> a.getStatus().name();
			case "studentId" -> a.getStudent().getId(); case "subjectId" -> a.getSubject().getId(); default -> a.getAttendanceDate();
		};
		return (Comparable<Object>) value;
	}

	@Transactional(readOnly = true)
	public List<AttendanceResponseDTO> byStudent(Long id) { requireStudent(id); return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(id).stream().map(this::toResponse).toList(); }
	@Transactional(readOnly = true)
	public List<AttendanceResponseDTO> bySubject(Long id) { requireSubject(id); return attendanceRepository.findBySubjectIdOrderByAttendanceDateDesc(id).stream().map(this::toResponse).toList(); }
	@Transactional(readOnly = true)
	public List<AttendanceResponseDTO> byDate(LocalDate date) { return attendanceRepository.findByAttendanceDateOrderByStudentId(date).stream().map(this::toResponse).toList(); }
	@Transactional(readOnly = true)
	public List<AttendanceResponseDTO> byRange(LocalDate start, LocalDate end) {
		if (start.isAfter(end)) throw new AttendanceException("startDate must not be after endDate", HttpStatus.BAD_REQUEST);
		return attendanceRepository.findByAttendanceDateBetweenOrderByAttendanceDateDesc(start, end).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AttendanceSummaryDTO studentSummary(Long id) { requireStudent(id); return summary(id, null); }
	@Transactional(readOnly = true)
	public AttendanceSummaryDTO subjectSummary(Long id) { requireSubject(id); return summary(null, id); }

	private AttendanceSummaryDTO summary(Long studentId, Long subjectId) {
		List<Attendance> records = attendanceRepository.findAll().stream().filter(a -> (studentId == null || a.getStudent().getId().equals(studentId)) && (subjectId == null || a.getSubject().getId().equals(subjectId))).toList();
		long present = records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
		long absent = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
		long late = records.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
		BigDecimal percentage = records.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(present + late).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
		return new AttendanceSummaryDTO(studentId, subjectId, records.size(), present, absent, late, percentage);
	}

	@Transactional
	public void delete(Long id) { attendanceRepository.delete(getEntity(id)); }

	private void ensureUnique(AttendanceRequestDTO r, Long id) {
		boolean exists = id == null ? attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDate(r.studentId(), r.subjectId(), r.attendanceDate()) : attendanceRepository.existsByStudentIdAndSubjectIdAndAttendanceDateAndIdNot(r.studentId(), r.subjectId(), r.attendanceDate(), id);
		if (exists) throw new AttendanceException("Attendance already exists for this student, subject and date", HttpStatus.CONFLICT);
	}
	private void validateDate(LocalDate date) {
		if (date != null && date.isAfter(LocalDate.now())) {
			throw new AttendanceException("attendanceDate cannot be in the future", HttpStatus.BAD_REQUEST);
		}
	}
	private Student getStudent(Long id) { return studentRepository.findById(id).orElseThrow(() -> new AttendanceException("Student not found: " + id, HttpStatus.NOT_FOUND)); }
	private Subject getSubject(Long id) { return subjectRepository.findById(id).orElseThrow(() -> new AttendanceException("Subject not found: " + id, HttpStatus.NOT_FOUND)); }
	private void requireStudent(Long id) { getStudent(id); }
	private void requireSubject(Long id) { getSubject(id); }
	private Attendance getEntity(Long id) { return attendanceRepository.findById(id).orElseThrow(() -> new AttendanceException("Attendance not found: " + id, HttpStatus.NOT_FOUND)); }
	private AttendanceResponseDTO toResponse(Attendance a) { return new AttendanceResponseDTO(a.getId(), a.getStudent().getId(), a.getStudent().getFirstName() + " " + a.getStudent().getLastName(), a.getSubject().getId(), a.getSubject().getName(), a.getAttendanceDate(), a.getStatus(), a.getRemarks(), a.getCreatedAt(), a.getUpdatedAt()); }
}
