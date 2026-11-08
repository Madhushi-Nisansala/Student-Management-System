package com.SMS.student.management.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class IntegrationTestSupport {
	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected long createStudent(String firstName, String lastName, String email) throws Exception {
		String response = mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
				.content("{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName
						+ "\",\"email\":\"" + email + "\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("id").asLong();
	}

	protected long createSubject(String code, String name) throws Exception {
		String response = mockMvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectCode\":\"" + code + "\",\"subjectName\":\"" + name
						+ "\",\"description\":\"Integration test subject\",\"credits\":3}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).get("id").asLong();
	}

	protected JsonNode json(String body) throws Exception {
		return objectMapper.readTree(body);
	}
}
