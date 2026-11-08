package com.SMS.student.management.system.marks;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.student_subject.StudentSubjectRepository;
import com.SMS.student.management.system.subject.Subject;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentMarkService {
	private static final int SCALE = 4;

	private final StudentMarkRepository repository;
	private final StudentRepository studentRepository;
	private final SubjectRepository subjectRepository;
	private final StudentSubjectRepository assignmentRepository;

	public StudentMarkService(StudentMarkRepository repository, StudentRepository studentRepository,
			SubjectRepository subjectRepository, StudentSubjectRepository assignmentRepository) {
		this.repository = repository;
		this.studentRepository = studentRepository;
		this.subjectRepository = subjectRepository;
		this.assignmentRepository = assignmentRepository;
	}

	public StudentMarkResponseDTO createMark(StudentMarkRequestDTO request) {
		Student student = getStudent(request.studentId());
		Subject subject = getSubject(request.subjectId());
		validateAssignment(request.studentId(), request.subjectId());
		validateMarkRange(request.marksObtained(), request.maximumMarks());
		if (repository.existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentType(request.studentId(), request.subjectId(), request.assessmentName(), request.assessmentType())) {
			throw conflict("An assessment with the same name and type already exists for this student and subject");
		}
		StudentMark mark = new StudentMark(student, subject, request.assessmentName(), request.assessmentType(),
				request.marksObtained(), request.maximumMarks(), request.assessmentDate(), request.remarks());
		calculateDerivedValues(mark);
		return save(mark);
	}

	public StudentMarkResponseDTO updateMark(Long id, StudentMarkRequestDTO request) {
		StudentMark mark = getEntity(id);
		Student student = getStudent(request.studentId());
		Subject subject = getSubject(request.subjectId());
		validateAssignment(request.studentId(), request.subjectId());
		validateMarkRange(request.marksObtained(), request.maximumMarks());
		if (repository.existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentTypeAndIdNot(request.studentId(), request.subjectId(), request.assessmentName(), request.assessmentType(), id)) {
			throw conflict("An assessment with the same name and type already exists for this student and subject");
		}
		mark.update(request.assessmentName(), request.assessmentType(), request.marksObtained(), request.maximumMarks(), request.assessmentDate(), request.remarks());
		mark.updateRelationships(student, subject);
		calculateDerivedValues(mark);
		return save(mark);
	}

	@Transactional(readOnly = true)
	public StudentMarkResponseDTO getMarkById(Long id) { return toResponse(getEntity(id)); }

	@Transactional(readOnly = true)
	public List<StudentMarkResponseDTO> getAllMarks() { return repository.findAll().stream().map(this::toResponse).toList(); }

	@Transactional(readOnly = true)
	public List<StudentMarkResponseDTO> getMarksByStudentId(Long studentId) {
		getStudent(studentId);
		return repository.findByStudentId(studentId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<StudentMarkResponseDTO> getMarksBySubjectId(Long subjectId) {
		getSubject(subjectId);
		return repository.findBySubjectId(subjectId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<StudentMarkResponseDTO> getMarksByStudentAndSubject(Long studentId, Long subjectId) {
		getStudent(studentId);
		getSubject(subjectId);
		return repository.findByStudentIdAndSubjectId(studentId, subjectId).stream().map(this::toResponse).toList();
	}

	public void deleteMark(Long id) { repository.delete(getEntity(id)); }

	public BigDecimal calculatePercentage(BigDecimal marksObtained, BigDecimal maximumMarks) {
		validateMarkRange(marksObtained, maximumMarks);
		return GradeCalculator.percentage(marksObtained, maximumMarks);
	}

	public String calculateGrade(BigDecimal percentage) {
		if (percentage == null) throw new MarksException("Percentage is required", HttpStatus.BAD_REQUEST);
		return GradeCalculator.grade(percentage);
	}

	@Transactional(readOnly = true)
	public StudentPerformanceDTO getStudentPerformance(Long studentId) {
		Student student = getStudent(studentId);
		List<StudentMark> marks = repository.findByStudentId(studentId);
		BigDecimal totalObtained = sum(marks, true);
		BigDecimal totalMaximum = sum(marks, false);
		BigDecimal overallPercentage = marks.isEmpty() ? zero() : percentage(totalObtained, totalMaximum);
		String overallGrade = marks.isEmpty() ? "N/A" : calculateGrade(overallPercentage);

		Map<Long, List<StudentMark>> bySubject = new LinkedHashMap<>();
		marks.forEach(mark -> bySubject.computeIfAbsent(mark.getSubject().getId(), ignored -> new ArrayList<>()).add(mark));
		List<SubjectPerformanceDTO> subjects = bySubject.values().stream().map(this::toSubjectPerformance).toList();
		return new StudentPerformanceDTO(student.getId(), fullName(student), marks.size(), totalObtained, totalMaximum,
				overallPercentage, overallGrade, subjects);
	}

	private SubjectPerformanceDTO toSubjectPerformance(List<StudentMark> marks) {
		Subject subject = marks.get(0).getSubject();
		BigDecimal obtained = sum(marks, true);
		BigDecimal maximum = sum(marks, false);
		BigDecimal average = percentage(obtained, maximum);
		return new SubjectPerformanceDTO(subject.getId(), subject.getSubjectCode(), subject.getSubjectName(), marks.size(), obtained, maximum, average, calculateGrade(average));
	}

	private StudentMarkResponseDTO save(StudentMark mark) {
		try {
			return toResponse(repository.save(mark));
		} catch (DataIntegrityViolationException ex) {
			throw conflict("An assessment with the same name and type already exists for this student and subject");
		}
	}

	private void calculateDerivedValues(StudentMark mark) {
		BigDecimal calculatedPercentage = calculatePercentage(mark.getMarksObtained(), mark.getMaximumMarks());
		mark.setCalculatedValues(calculatedPercentage, calculateGrade(calculatedPercentage));
	}

	private void validateMarkRange(BigDecimal obtained, BigDecimal maximum) {
		if (obtained == null || maximum == null || maximum.signum() <= 0 || obtained.signum() < 0) {
			throw new MarksException("Marks obtained must be non-negative and maximum marks must be greater than zero", HttpStatus.BAD_REQUEST);
		}
		if (obtained.compareTo(maximum) > 0) {
			throw new MarksException("Marks obtained must not exceed maximum marks", HttpStatus.BAD_REQUEST);
		}
	}

	private void validateAssignment(Long studentId, Long subjectId) {
		if (!assignmentRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
			throw new MarksException("Student must be assigned to the subject before marks can be recorded", HttpStatus.CONFLICT);
		}
	}

	private Student getStudent(Long id) {
		return studentRepository.findById(id).orElseThrow(() -> new MarksException("Student was not found", HttpStatus.NOT_FOUND));
	}

	private Subject getSubject(Long id) {
		return subjectRepository.findById(id).orElseThrow(() -> new MarksException("Subject was not found", HttpStatus.NOT_FOUND));
	}

	private StudentMark getEntity(Long id) {
		return repository.findById(id).orElseThrow(() -> new MarksException("Mark was not found", HttpStatus.NOT_FOUND));
	}

	private StudentMarkResponseDTO toResponse(StudentMark mark) {
		Student student = mark.getStudent();
		Subject subject = mark.getSubject();
		return new StudentMarkResponseDTO(mark.getId(), student.getId(), fullName(student), subject.getId(), subject.getSubjectCode(), subject.getSubjectName(),
				mark.getAssessmentName(), mark.getAssessmentType(), mark.getMarksObtained(), mark.getMaximumMarks(), mark.getPercentage(), mark.getGrade(),
				mark.getAssessmentDate(), mark.getRemarks(), mark.getCreatedAt(), mark.getUpdatedAt());
	}

	private BigDecimal sum(List<StudentMark> marks, boolean obtained) {
		return marks.stream().map(mark -> obtained ? mark.getMarksObtained() : mark.getMaximumMarks()).reduce(zero(), BigDecimal::add);
	}

	private BigDecimal percentage(BigDecimal obtained, BigDecimal maximum) {
		return obtained.multiply(BigDecimal.valueOf(100)).divide(maximum, SCALE, RoundingMode.HALF_UP);
	}

	private BigDecimal zero() { return BigDecimal.ZERO.setScale(SCALE); }
	private String fullName(Student student) { return student.getFirstName() + " " + student.getLastName(); }
	private MarksException conflict(String message) { return new MarksException(message, HttpStatus.CONFLICT); }
}