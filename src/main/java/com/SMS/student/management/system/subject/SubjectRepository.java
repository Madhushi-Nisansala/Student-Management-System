package com.SMS.student.management.system.subject;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
	boolean existsBySubjectCode(String subjectCode);

	boolean existsBySubjectCodeAndIdNot(String subjectCode, Long id);
}
