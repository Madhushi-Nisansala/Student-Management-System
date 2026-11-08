package com.SMS.student.management.system;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class BackendApiIntegrationTest extends IntegrationTestSupport {
	@Test
	void studentCrudAndValidationUseTheRealPersistenceLayer() throws Exception {
		long studentId = createStudent("Ada", "Lovelace", "ada.integration@example.com");
		mockMvc.perform(get("/api/students/{id}", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Ada"));
		mockMvc.perform(get("/api/students")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("ada.integration@example.com"));
		mockMvc.perform(put("/api/students/{id}", studentId).contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"Augusta\",\"lastName\":\"King\",\"email\":\"augusta.integration@example.com\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.lastName").value("King"));
		mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"Duplicate\",\"lastName\":\"Email\",\"email\":\"augusta.integration@example.com\"}"))
				.andExpect(status().isConflict()).andExpect(content().string(containsString("already registered")));
		mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"Bad\",\"lastName\":\"Email\",\"email\":\"not-an-email\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
		mockMvc.perform(get("/api/students/999999")).andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/students/{id}", studentId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/students/{id}", studentId)).andExpect(status().isNotFound());
	}

	@Test
	void subjectCrudValidationDuplicateAndAssignedDeleteProtectionWork() throws Exception {
		long subjectId = createSubject("INT-101", "Integration Testing");
		mockMvc.perform(get("/api/subjects/{id}", subjectId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.subjectCode").value("INT-101"));
		mockMvc.perform(put("/api/subjects/{id}", subjectId).contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectCode\":\"INT-102\",\"subjectName\":\"Advanced Integration\",\"description\":\"Updated\",\"credits\":4}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.credits").value(4));
		long duplicateId = createSubject("INT-103", "Other Subject");
		mockMvc.perform(put("/api/subjects/{id}", duplicateId).contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectCode\":\"INT-102\",\"subjectName\":\"Duplicate\",\"description\":\"\",\"credits\":3}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectCode\":\"BAD\",\"subjectName\":\"Bad Credits\",\"description\":\"\",\"credits\":0}"))
				.andExpect(status().isBadRequest());
		long studentId = createStudent("Grace", "Hopper", "grace.integration@example.com");
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId))
				.andExpect(status().isCreated());
		mockMvc.perform(delete("/api/subjects/{id}", subjectId)).andExpect(status().isConflict());
		mockMvc.perform(get("/api/subjects/999999")).andExpect(status().isNotFound());
	}

	@Test
	void assignmentLifecycleKeepsStudentAndSubjectRecords() throws Exception {
		long studentId = createStudent("Alan", "Turing", "alan.integration@example.com");
		long subjectId = createSubject("ALG-101", "Algorithms");
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.studentId").value(studentId))
				.andExpect(jsonPath("$.subjectId").value(subjectId));
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId))
				.andExpect(status().isConflict());
		mockMvc.perform(get("/api/students/{studentId}/subjects", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].subjectCode").value("ALG-101"));
		mockMvc.perform(get("/api/subjects/{subjectId}/students", subjectId)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].studentName").value("Alan Turing"));
		mockMvc.perform(delete("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/students/{studentId}", studentId)).andExpect(status().isOk());
		mockMvc.perform(get("/api/subjects/{subjectId}", subjectId)).andExpect(status().isOk());
		mockMvc.perform(post("/api/students/999999/subjects/{subjectId}", subjectId))
				.andExpect(status().isNotFound());
	}

	@Test
	void attendanceEndpointsValidateStatusesDatesDuplicatesPaginationAndSummary() throws Exception {
		long studentId = createStudent("Katherine", "Johnson", "katherine.integration@example.com");
		long subjectId = createSubject("ATT-101", "Attendance");
		String base = "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"attendanceDate\":\"%s\",\"status\":\"%s\",\"remarks\":\"checked\"}";
		long attendanceId = json(mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now().minusDays(2), "PRESENT"))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now().minusDays(1), "ABSENT"))).andExpect(status().isCreated());
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now(), "LATE"))).andExpect(status().isCreated());
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now().minusDays(2), "PRESENT"))).andExpect(status().isConflict());
		mockMvc.perform(get("/api/attendance").param("studentId", String.valueOf(studentId)).param("page", "0").param("size", "2"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.content.length()").value(2));
		mockMvc.perform(get("/api/attendance/summary/student/{id}", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.totalDays").value(3)).andExpect(jsonPath("$.presentDays").value(1))
				.andExpect(jsonPath("$.absentDays").value(1)).andExpect(jsonPath("$.lateDays").value(1))
				.andExpect(jsonPath("$.attendancePercentage").value(66.67));
		mockMvc.perform(put("/api/attendance/{id}", attendanceId).contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now().minusDays(2), "LATE"))).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("LATE"));
		mockMvc.perform(post("/api/attendance").contentType(MediaType.APPLICATION_JSON)
				.content(base.formatted(LocalDate.now().plusDays(1), "PRESENT"))).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/attendance/range").param("startDate", LocalDate.now().toString()).param("endDate", LocalDate.now().minusDays(1).toString()))
				.andExpect(status().isBadRequest());
		mockMvc.perform(delete("/api/attendance/{id}", attendanceId)).andExpect(status().isNoContent());
	}
}
