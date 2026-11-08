package com.SMS.student.management.system;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.SMS.student.management.system.attendance.AttendanceRepository;
import com.SMS.student.management.system.marks.StudentMarkRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class EndpointCoverageIntegrationTest extends IntegrationTestSupport {
	@Autowired
	private AttendanceRepository attendanceRepository;

	@Autowired
	private StudentMarkRepository markRepository;

	@Test
	void coversAllAttendanceRetrievalRoutesFiltersAndSubjectSummary() throws Exception {
		long studentId = createStudent("Katherine", "Johnson", "routes.integration@example.com");
		long subjectId = createSubject("ROU-101", "Route Coverage");
		long firstId = createAttendance(studentId, subjectId, LocalDate.now().minusDays(3), "PRESENT");
		createAttendance(studentId, subjectId, LocalDate.now().minusDays(2), "ABSENT");
		createAttendance(studentId, subjectId, LocalDate.now().minusDays(1), "LATE");

		mockMvc.perform(get("/api/attendance/{id}", firstId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(firstId)).andExpect(jsonPath("$.status").value("PRESENT"));
		mockMvc.perform(get("/api/attendance/student/{id}", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/attendance/subject/{id}", subjectId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/attendance/date/{date}", LocalDate.now().minusDays(2)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(get("/api/attendance/range").param("startDate", LocalDate.now().minusDays(3).toString())
				.param("endDate", LocalDate.now().minusDays(1).toString())).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/attendance").param("studentId", String.valueOf(studentId))
				.param("subjectId", String.valueOf(subjectId)).param("status", "LATE")
				.param("date", LocalDate.now().minusDays(1).toString()).param("sort", "attendanceDate,asc"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].status").value("LATE"));
		mockMvc.perform(get("/api/attendance/summary/subject/{id}", subjectId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.subjectId").value(subjectId)).andExpect(jsonPath("$.totalDays").value(3))
				.andExpect(jsonPath("$.presentDays").value(1)).andExpect(jsonPath("$.absentDays").value(1))
				.andExpect(jsonPath("$.lateDays").value(1)).andExpect(jsonPath("$.attendancePercentage").value(66.67));
		mockMvc.perform(get("/api/attendance/summary/student/{id}", 999999L)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/attendance/subject/{id}", 999999L)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/attendance/{id}", 999999L)).andExpect(status().isNotFound());
		assertRepositoryState(firstId, studentId, subjectId);
	}

	@Test
	void coversAttendanceEmptyResultsAndInvalidParameters() throws Exception {
		long studentId = createStudent("Empty", "Attendance", "empty.attendance.integration@example.com");
		mockMvc.perform(get("/api/attendance/summary/student/{id}", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalDays").value(0)).andExpect(jsonPath("$.attendancePercentage").value(0));
		mockMvc.perform(get("/api/attendance").param("page", "not-a-number"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
		mockMvc.perform(get("/api/attendance").param("status", "UNKNOWN"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
		mockMvc.perform(get("/api/attendance/range").param("startDate", "not-a-date").param("endDate", LocalDate.now().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void validatesRequiredFieldsInvalidJsonAndInvalidAttendanceStatus() throws Exception {
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON).content("not-json"))
				.andExpect(status().isBadRequest()).andExpect(content().string(containsString("Invalid request")));
		long studentId = createStudent("Invalid", "Attendance", "invalid.attendance.integration@example.com");
		long subjectId = createSubject("INV-101", "Invalid Attendance");
		String body = "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId
				+ ",\"attendanceDate\":\"2025-01-01\",\"status\":\"INVALID\"}";
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(validAttendanceJson(999999L, subjectId, LocalDate.now(), "PRESENT")))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(validAttendanceJson(studentId, 999999L, LocalDate.now(), "PRESENT")))
				.andExpect(status().isNotFound());
	}

	@Test
	void validatesStudentSubjectAndMarkRequiredFieldsAndReferences() throws Exception {
		mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectCode\":\"\",\"subjectName\":\"\",\"credits\":null}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/students/{id}/subjects", 999999L)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/subjects/{id}/students", 999999L)).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", 999999L, 999999L))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content("not-json")).andExpect(status().isBadRequest());
	}

	@Test
	void verifiesMarkTimestampsAndUpdatedValuesReachTheRepository() throws Exception {
		long studentId = createStudent("Timestamp", "Tester", "timestamp.integration@example.com");
		long subjectId = createSubject("TIM-101", "Timestamp Testing");
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId))
				.andExpect(status().isCreated());
		String created = mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Timestamp Quiz", "QUIZ", "7", "10")))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.updatedAt").isNotEmpty()).andReturn().getResponse().getContentAsString();
		long markId = json(created).get("id").asLong();
		var saved = markRepository.findById(markId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertNotNull(saved.getCreatedAt());
		org.junit.jupiter.api.Assertions.assertNotNull(saved.getUpdatedAt());
		mockMvc.perform(put("/api/marks/{id}", markId).contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Timestamp Quiz Updated", "QUIZ", "9", "10")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.assessmentName").value("Timestamp Quiz Updated"));
		var updated = markRepository.findById(markId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("Timestamp Quiz Updated", updated.getAssessmentName());
		org.junit.jupiter.api.Assertions.assertNotNull(updated.getUpdatedAt());
	}

	private long createAttendance(long studentId, long subjectId, LocalDate date, String status) throws Exception {
		String response = mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(validAttendanceJson(studentId, subjectId, date, status))).andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return json(response).get("id").asLong();
	}

	private String validAttendanceJson(long studentId, long subjectId, LocalDate date, String status) {
		return "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"attendanceDate\":\"" + date
				+ "\",\"status\":\"" + status + "\",\"remarks\":\"coverage\"}";
	}

	private String markJson(long studentId, long subjectId, String name, String type, String obtained, String maximum) {
		return "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"assessmentName\":\"" + name
				+ "\",\"assessmentType\":\"" + type + "\",\"marksObtained\":" + obtained + ",\"maximumMarks\":" + maximum
				+ ",\"assessmentDate\":\"" + LocalDate.now().minusDays(1) + "\"}";
	}

	private void assertRepositoryState(long attendanceId, long studentId, long subjectId) {
		var attendance = attendanceRepository.findById(attendanceId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(studentId, attendance.getStudent().getId());
		org.junit.jupiter.api.Assertions.assertEquals(subjectId, attendance.getSubject().getId());
		org.junit.jupiter.api.Assertions.assertNotNull(attendance.getCreatedAt());
		org.junit.jupiter.api.Assertions.assertNotNull(attendance.getUpdatedAt());
	}
}
