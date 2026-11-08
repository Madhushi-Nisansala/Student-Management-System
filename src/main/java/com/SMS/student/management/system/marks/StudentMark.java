package com.SMS.student.management.system.marks;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_marks", uniqueConstraints = @UniqueConstraint(
		name = "uk_student_mark_assessment", columnNames = {"student_id", "subject_id", "assessment_name", "assessment_type"}),
		indexes = {
			@Index(name = "idx_student_marks_student", columnList = "student_id"),
			@Index(name = "idx_student_marks_subject", columnList = "subject_id"),
			@Index(name = "idx_student_marks_assessment_date", columnList = "assessment_date")
		})
public class StudentMark {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	@NotBlank
	@Size(max = 120)
	@Column(name = "assessment_name", nullable = false, length = 120)
	private String assessmentName;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "assessment_type", nullable = false, length = 20)
	private AssessmentType assessmentType;

	@NotNull
	@DecimalMin(value = "0.0")
	@Column(name = "marks_obtained", nullable = false, precision = 10, scale = 4)
	private BigDecimal marksObtained;

	@NotNull
	@DecimalMin(value = "0.0", inclusive = false)
	@Column(name = "maximum_marks", nullable = false, precision = 10, scale = 4)
	private BigDecimal maximumMarks;

	@Column(precision = 7, scale = 4)
	private BigDecimal percentage;

	@Column(length = 2)
	private String grade;

	@Size(max = 500)
	@Column(length = 500)
	private String remarks;

	@NotNull
	@Column(name = "assessment_date", nullable = false)
	private LocalDate assessmentDate;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected StudentMark() { }

	public StudentMark(Student student, Subject subject, String assessmentName, AssessmentType assessmentType,
			BigDecimal marksObtained, BigDecimal maximumMarks, LocalDate assessmentDate, String remarks) {
		this.student = student;
		this.subject = subject;
		this.assessmentName = assessmentName;
		this.assessmentType = assessmentType;
		this.marksObtained = marksObtained;
		this.maximumMarks = maximumMarks;
		this.assessmentDate = assessmentDate;
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
	public String getAssessmentName() { return assessmentName; }
	public AssessmentType getAssessmentType() { return assessmentType; }
	public BigDecimal getMarksObtained() { return marksObtained; }
	public BigDecimal getMaximumMarks() { return maximumMarks; }
	public BigDecimal getPercentage() { return percentage; }
	public String getGrade() { return grade; }
	public String getRemarks() { return remarks; }
	public LocalDate getAssessmentDate() { return assessmentDate; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }

	public void setCalculatedValues(BigDecimal percentage, String grade) {
		this.percentage = percentage;
		this.grade = grade;
	}

	public void update(String assessmentName, AssessmentType assessmentType, BigDecimal marksObtained,
			BigDecimal maximumMarks, LocalDate assessmentDate, String remarks) {
		this.assessmentName = assessmentName;
		this.assessmentType = assessmentType;
		this.marksObtained = marksObtained;
		this.maximumMarks = maximumMarks;
		this.assessmentDate = assessmentDate;
		this.remarks = remarks;
	}

	public void updateRelationships(Student student, Subject subject) {
		this.student = student;
		this.subject = subject;
	}
}