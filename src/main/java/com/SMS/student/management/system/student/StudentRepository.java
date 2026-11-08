package com.SMS.student.management.system.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);
}
