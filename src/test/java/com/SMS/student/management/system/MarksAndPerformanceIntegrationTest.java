package com.SMS.student.management.system;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MarksAndPerformanceIntegrationTest extends IntegrationTestSupport {
	@Test
	void marksWorkflowFiltersUpdatesCalculatesWeightedPerformanceAndDeletes() throws Exception {
		long studentId = createStudent("Dorothy", "Vaughan", "dorothy.integration@example.com");
		long subjectId = createSubject("MAT-101", "Mathematics");
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId)).andExpect(status().isCreated());
		long assignmentId = createMark(studentId, subjectId, "Assignment 1", "ASSIGNMENT", "9", "10");
		long quizId = createMark(studentId, subjectId, "Quiz 1", "QUIZ", "8", "10");
		createMark(studentId, subjectId, "Mid-term", "MID_TERM", "17", "20");
		mockMvc.perform(get("/api/marks")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/marks/{id}", assignmentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.percentage").value(90.0)).andExpect(jsonPath("$.grade").value("A+"));
		mockMvc.perform(get("/api/marks/student/{studentId}", studentId)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/marks/subject/{subjectId}", subjectId)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/marks/student/{studentId}/subject/{subjectId}", studentId, subjectId))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
		mockMvc.perform(get("/api/marks/student/{studentId}/performance", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.studentId").value(studentId)).andExpect(jsonPath("$.studentName").value("Dorothy Vaughan"))
				.andExpect(jsonPath("$.assessmentCount").value(3)).andExpect(jsonPath("$.totalMarksObtained").value(34.0))
				.andExpect(jsonPath("$.totalMaximumMarks").value(40.0)).andExpect(jsonPath("$.overallAveragePercentage").value(85.0))
				.andExpect(jsonPath("$.overallGrade").value("A")).andExpect(jsonPath("$.subjectPerformance[0].averagePercentage").value(85.0));
		mockMvc.perform(put("/api/marks/{id}", quizId).contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Quiz 1 Updated", "QUIZ", "10", "10")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.grade").value("A+"));
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Assignment 1", "ASSIGNMENT", "9", "10")))
				.andExpect(status().isConflict()).andExpect(content().string(containsString("same name and type")));
		mockMvc.perform(delete("/api/marks/{id}", assignmentId)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/marks/{id}", assignmentId)).andExpect(status().isNotFound());
	}

	@Test
	void marksRejectInvalidRangeAssessmentValuesAndMissingAssignments() throws Exception {
		long studentId = createStudent("Evelyn", "Boyd", "evelyn.integration@example.com");
		long subjectId = createSubject("PHY-101", "Physics");
		String valid = markJson(studentId, subjectId, "Quiz", "QUIZ", "5", "10");
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON).content(valid)).andExpect(status().isConflict());
		mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId)).andExpect(status().isCreated());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Negative", "QUIZ", "-1", "10"))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Too High", "QUIZ", "11", "10"))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "Zero Max", "QUIZ", "0", "0"))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, "", "QUIZ", "1", "10"))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content("{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"assessmentName\":\"Bad Type\",\"assessmentType\":\"INVALID\",\"marksObtained\":1,\"maximumMarks\":10,\"assessmentDate\":\"2025-01-01\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/marks/999999")).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/marks/student/999999")).andExpect(status().isNotFound());
	}

	@Test
	void performanceForStudentWithoutMarksIsSafeAndInvalidStudentIsNotFound() throws Exception {
		long studentId = createStudent("Mary", "Jackson", "mary.integration@example.com");
		mockMvc.perform(get("/api/marks/student/{studentId}/performance", studentId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.assessmentCount").value(0)).andExpect(jsonPath("$.overallAveragePercentage").value(0.0))
				.andExpect(jsonPath("$.overallGrade").value("N/A")).andExpect(jsonPath("$.subjectPerformance").isEmpty());
		mockMvc.perform(get("/api/marks/student/999999/performance")).andExpect(status().isNotFound());
	}

	@Test
	void performanceEndpointReturnsEveryGradeBoundary() throws Exception {
		Map<Integer, String> boundaries = new LinkedHashMap<>();
		boundaries.put(95, "A+"); boundaries.put(90, "A+"); boundaries.put(89, "A"); boundaries.put(80, "A");
		boundaries.put(79, "B"); boundaries.put(70, "B"); boundaries.put(69, "C"); boundaries.put(60, "C");
		boundaries.put(59, "D"); boundaries.put(50, "D"); boundaries.put(49, "F"); boundaries.put(0, "F");
		int index = 0;
		for (Map.Entry<Integer, String> boundary : boundaries.entrySet()) {
			long studentId = createStudent("Boundary", "Student" + index, "boundary" + index + "@integration.example.com");
			long subjectId = createSubject("GRD-" + (100 + index), "Grade " + index);
			mockMvc.perform(post("/api/students/{studentId}/subjects/{subjectId}", studentId, subjectId)).andExpect(status().isCreated());
			createMark(studentId, subjectId, "Boundary", "QUIZ", String.valueOf(boundary.getKey()), "100");
			mockMvc.perform(get("/api/marks/student/{studentId}/performance", studentId)).andExpect(status().isOk())
					.andExpect(jsonPath("$.overallGrade").value(boundary.getValue()));
			index++;
		}
	}

	private long createMark(long studentId, long subjectId, String name, String type, String obtained, String maximum) throws Exception {
		String response = mockMvc.perform(post("/api/marks").contentType(MediaType.APPLICATION_JSON)
				.content(markJson(studentId, subjectId, name, type, obtained, maximum))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return json(response).get("id").asLong();
	}

	private String markJson(long studentId, long subjectId, String name, String type, String obtained, String maximum) {
		return "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"assessmentName\":\"" + name
				+ "\",\"assessmentType\":\"" + type + "\",\"marksObtained\":" + obtained + ",\"maximumMarks\":" + maximum
				+ ",\"assessmentDate\":\"" + LocalDate.now().minusDays(1) + "\",\"remarks\":\"integration\"}";
	}
}
