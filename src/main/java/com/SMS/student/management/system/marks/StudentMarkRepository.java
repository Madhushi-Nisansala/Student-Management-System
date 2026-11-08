package com.SMS.student.management.system.marks;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {
	List<StudentMark> findByStudentId(Long studentId);
	List<StudentMark> findBySubjectId(Long subjectId);
	List<StudentMark> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
	List<StudentMark> findByAssessmentType(AssessmentType assessmentType);
	boolean existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentType(
			Long studentId, Long subjectId, String assessmentName, AssessmentType assessmentType);
	boolean existsByStudentIdAndSubjectIdAndAssessmentNameAndAssessmentTypeAndIdNot(
			Long studentId, Long subjectId, String assessmentName, AssessmentType assessmentType, Long id);
	Optional<StudentMark> findById(Long id);
}