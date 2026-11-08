package com.SMS.student.management.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.SMS.student.management.system.attendance.AttendanceRepository;
import com.SMS.student.management.system.marks.StudentMarkRepository;
import com.SMS.student.management.system.student.StudentRepository;
import com.SMS.student.management.system.student_subject.StudentSubjectRepository;
import com.SMS.student.management.system.subject.SubjectRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CompleteStudentManagementWorkflowTest extends IntegrationTestSupport {
	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private StudentSubjectRepository assignmentRepository;

	@Autowired
	private AttendanceRepository attendanceRepository;

	@Autowired
	private StudentMarkRepository markRepository;

	@Test
	void completeWorkflowPersistsRelationshipsAndUsesSafeDeletionOrder() throws Exception {
		long studentId = createStudent("Barbara", "Liskov", "barbara.integration@example.com");
		long subjectId = createSubject("E2E-101", "End to End Testing");
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId)).andExpect(status().isCreated());
		assertTrue(assignmentRepository.existsByStudentIdAndSubjectId(studentId, subjectId));
		assertTrue(studentRepository.findById(studentId).isPresent());
		assertTrue(subjectRepository.findById(subjectId).isPresent());

		String attendance = "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"attendanceDate\":\"" + LocalDate.now().minusDays(1)
				+ "\",\"status\":\"PRESENT\",\"remarks\":\"workflow\"}";
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON).content(attendance)).andExpect(status().isCreated());
		String marks = "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"assessmentName\":\"Workflow Assignment\",\"assessmentType\":\"ASSIGNMENT\",\"marksObtained\":18,\"maximumMarks\":20,\"assessmentDate\":\"" + LocalDate.now().minusDays(1) + "\"}";
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON).content(marks)).andExpect(status().isCreated());
		assertEquals(1, attendanceRepository.countByStudentId(studentId));
		assertEquals(1, markRepository.findByStudentId(studentId).size());
		assertEquals(1, markRepository.findBySubjectId(subjectId).size());

		mockMvc.perform(delete("/api/subjects/{id}", subjectId)).andExpect(status().isConflict());
		mockMvc.perform(delete("/api/marks/{id}", markRepository.findByStudentId(studentId).get(0).getId())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/attendance/{id}", attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId).get(0).getId())).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId)).andExpect(status().isNoContent());
		assertTrue(studentRepository.findById(studentId).isPresent());
		assertTrue(subjectRepository.findById(subjectId).isPresent());
		mockMvc.perform(delete("/api/subjects/{id}", subjectId)).andExpect(status().isNoContent());
		assertTrue(studentRepository.findById(studentId).isPresent());
	}
}
