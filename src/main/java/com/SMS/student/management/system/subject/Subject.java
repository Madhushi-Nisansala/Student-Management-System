package com.SMS.student.management.system.subject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
public class Subject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 120)
	@Column(name = "name", nullable = false, length = 120)
	private String subjectName;

	@NotBlank
	@Size(max = 30)
	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String subjectCode;

	@Size(max = 1000)
	@Column(length = 1000)
	private String description;

	@Positive
	@Column
	private Integer credits;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	protected Subject() { }

	public Subject(String name, String code) {
		this.subjectName = name;
		this.subjectCode = code;
	}

	public Subject(String subjectCode, String subjectName, String description, Integer credits) {
		this.subjectCode = subjectCode;
		this.subjectName = subjectName;
		this.description = description;
		this.credits = credits;
	}

	@PrePersist
	void onCreate() { createdAt = LocalDateTime.now(); }

	public Long getId() { return id; }
	public String getSubjectName() { return subjectName; }
	public String getSubjectCode() { return subjectCode; }
	public String getDescription() { return description; }
	public Integer getCredits() { return credits; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public String getName() { return subjectName; }
	public String getCode() { return subjectCode; }
	public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
	public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
	public void setDescription(String description) { this.description = description; }
	public void setCredits(Integer credits) { this.credits = credits; }
	public void setName(String name) { this.subjectName = name; }
	public void setCode(String code) { this.subjectCode = code; }
}
