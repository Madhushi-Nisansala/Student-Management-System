package com.SMS.student.management.system.student;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class StudentControllerTest {
	private StudentRepository repository;
	private MockMvc mockMvc;
	private Student savedStudent;

	@BeforeEach
	void setUp() {
		repository = Mockito.mock(StudentRepository.class);
		savedStudent = new Student("Kamal", "Perera", "kamal.perera@example.com");
		mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(repository))
				.setControllerAdvice(new com.SMS.student.management.system.common.GlobalExceptionHandler())
				.setValidator(new LocalValidatorFactoryBean())
				.setMessageConverters(new JacksonJsonHttpMessageConverter())
				.build();
	}

	@Test
	void createsStudent() throws Exception {
		when(repository.existsByEmail("kamal.perera@example.com")).thenReturn(false);
		when(repository.save(any(Student.class))).thenReturn(savedStudent);

		mockMvc.perform(post("/api/students").contentType("application/json")
				.content("{\"firstName\":\"Kamal\",\"lastName\":\"Perera\",\"email\":\"kamal.perera@example.com\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.firstName").value("Kamal"));
	}

	@Test
	void getsAllStudents() throws Exception {
		when(repository.findAll()).thenReturn(List.of(savedStudent));
		mockMvc.perform(get("/api/students")).andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("kamal.perera@example.com"));
	}

	@Test
	void getsStudentById() throws Exception {
		when(repository.findById(1L)).thenReturn(Optional.of(savedStudent));
		mockMvc.perform(get("/api/students/1")).andExpect(status().isOk()).andExpect(jsonPath("$.lastName").value("Perera"));
	}

	@Test
	void returnsNotFoundForUnknownStudent() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/students/99")).andExpect(status().isNotFound());
	}

	@Test
	void updatesStudent() throws Exception {
		when(repository.findById(1L)).thenReturn(Optional.of(savedStudent));
		when(repository.existsByEmailAndIdNot("kamal.silva@example.com", 1L)).thenReturn(false);
		when(repository.save(savedStudent)).thenReturn(savedStudent);

		mockMvc.perform(put("/api/students/1").contentType("application/json")
				.content("{\"firstName\":\"Kamal\",\"lastName\":\"Silva\",\"email\":\"kamal.silva@example.com\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void returnsNotFoundWhenUpdatingUnknownStudent() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		mockMvc.perform(put("/api/students/99").contentType("application/json")
				.content("{\"firstName\":\"Kamal\",\"lastName\":\"Silva\",\"email\":\"kamal.silva@example.com\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deletesStudent() throws Exception {
		when(repository.findById(1L)).thenReturn(Optional.of(savedStudent));
		doNothing().when(repository).deleteById(1L);
		mockMvc.perform(delete("/api/students/1")).andExpect(status().isNoContent());
		verify(repository).deleteById(1L);
	}

	@Test
	void returnsNotFoundWhenDeletingUnknownStudent() throws Exception {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		mockMvc.perform(delete("/api/students/99")).andExpect(status().isNotFound());
		verify(repository, never()).deleteById(99L);
	}

	@Test
	void rejectsMissingFirstName() throws Exception {
		invalidCreate("{\"lastName\":\"Perera\",\"email\":\"valid@example.com\"}");
	}

	@Test
	void rejectsMissingLastName() throws Exception {
		invalidCreate("{\"firstName\":\"Kamal\",\"email\":\"valid@example.com\"}");
	}

	@Test
	void rejectsInvalidEmail() throws Exception {
		invalidCreate("{\"firstName\":\"Kamal\",\"lastName\":\"Perera\",\"email\":\"not-an-email\"}");
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		when(repository.existsByEmail("kamal.perera@example.com")).thenReturn(true);
		mockMvc.perform(post("/api/students").contentType("application/json")
				.content("{\"firstName\":\"Another\",\"lastName\":\"Student\",\"email\":\"kamal.perera@example.com\"}"))
				.andExpect(status().isConflict())
				.andExpect(content().string(containsString("Email is already registered")));
	}

	private void invalidCreate(String body) throws Exception {
		mockMvc.perform(post("/api/students").contentType("application/json").content(body))
				.andExpect(status().isBadRequest());
	}
}
