package com.SMS.student.management.system.student_subject;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.subject.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_subjects", uniqueConstraints = @UniqueConstraint(
		name = "uk_student_subject", columnNames = {"student_id", "subject_id"}))
public class StudentSubject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	@Column(nullable = false, updatable = false)
	private LocalDateTime assignedAt;

	protected StudentSubject() { }

	public StudentSubject(Student student, Subject subject) {
		this.student = student;
		this.subject = subject;
	}

	@PrePersist
	void onCreate() { assignedAt = LocalDateTime.now(); }

	public Long getId() { return id; }
	public Student getStudent() { return student; }
	public Subject getSubject() { return subject; }
	public LocalDateTime getAssignedAt() { return assignedAt; }
}