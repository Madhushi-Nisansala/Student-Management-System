package com.SMS.student.management.system.student_subject;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSubjectRepository extends JpaRepository<StudentSubject, Long> {
	boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);
	boolean existsBySubjectId(Long subjectId);
	Optional<StudentSubject> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
	List<StudentSubject> findByStudentId(Long studentId);
	List<StudentSubject> findBySubjectId(Long subjectId);
}