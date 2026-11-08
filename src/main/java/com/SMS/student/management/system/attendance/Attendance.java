package com.SMS.student.management.system.attendance;

import com.SMS.student.management.system.student.Student;
import com.SMS.student.management.system.subject.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(name = "uk_attendance_student_subject_date", columnNames = {"student_id", "subject_id", "attendance_date"}), indexes = {
		@Index(name = "idx_attendance_student_date", columnList = "student_id, attendance_date"),
		@Index(name = "idx_attendance_subject_date", columnList = "subject_id, attendance_date"),
		@Index(name = "idx_attendance_date", columnList = "attendance_date")
})
public class Attendance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	@Column(name = "attendance_date", nullable = false)
	private LocalDate attendanceDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private AttendanceStatus status;

	@Column(length = 500)
	private String remarks;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected Attendance() { }

	public Attendance(Student student, Subject subject, LocalDate attendanceDate, AttendanceStatus status, String remarks) {
		this.student = student;
		this.subject = subject;
		this.attendanceDate = attendanceDate;
		this.status = status;
		this.remarks = remarks;
	}

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() { updatedAt = LocalDateTime.now(); }

	public Long getId() { return id; }
	public Student getStudent() { return student; }
	public Subject getSubject() { return subject; }
	public LocalDate getAttendanceDate() { return attendanceDate; }
	public AttendanceStatus getStatus() { return status; }
	public String getRemarks() { return remarks; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void update(LocalDate date, AttendanceStatus status, String remarks) {
		this.attendanceDate = date;
		this.status = status;
		this.remarks = remarks;
	}

	public void updateRelationships(Student student, Subject subject) {
		this.student = student;
		this.subject = subject;
	}
}
