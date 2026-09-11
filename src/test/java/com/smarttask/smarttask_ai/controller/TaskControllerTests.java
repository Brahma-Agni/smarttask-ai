package com.smarttask.smarttask_ai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.smarttask.smarttask_ai.entity.Task;
import com.smarttask.smarttask_ai.service.TaskService;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void blankTitleAndDescriptionFailValidation() throws Exception {
		mockMvc.perform(post("/tasks")
				.param("title", " ")
				.param("description", " "))
				.andExpect(status().isOk())
				.andExpect(view().name("task-form"))
				.andExpect(model().attributeHasFieldErrors("task", "title", "description"));

		verify(taskService, never()).createTask(any(Task.class));
	}

	@Test
	void titleOverMaximumLengthFailsValidation() throws Exception {
		mockMvc.perform(post("/tasks")
				.param("title", "T".repeat(121))
				.param("description", "Valid description"))
				.andExpect(status().isOk())
				.andExpect(view().name("task-form"))
				.andExpect(model().attributeHasFieldErrors("task", "title"));

		verify(taskService, never()).createTask(any(Task.class));
	}

	@Test
	void descriptionOverMaximumLengthFailsValidation() throws Exception {
		mockMvc.perform(post("/tasks")
				.param("title", "Valid title")
				.param("description", "D".repeat(2001)))
				.andExpect(status().isOk())
				.andExpect(view().name("task-form"))
				.andExpect(model().attributeHasFieldErrors("task", "description"));

		verify(taskService, never()).createTask(any(Task.class));
	}

	@Test
	void validCreateStillSucceeds() throws Exception {
		mockMvc.perform(post("/tasks")
				.param("title", "Review payment report")
				.param("description", "Review the latest payment report."))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/tasks"));

		verify(taskService).createTask(any(Task.class));
	}

	@Test
	void invalidEditPreservesMetadataAndDoesNotUpdateTask() throws Exception {
		Task existingTask = existingTask();
		when(taskService.getTaskById(1L)).thenReturn(existingTask);

		mockMvc.perform(post("/tasks/1")
				.param("title", " ")
				.param("description", "Updated description"))
				.andExpect(status().isOk())
				.andExpect(view().name("task-form"))
				.andExpect(model().attributeHasFieldErrors("task", "title"))
				.andExpect(model().attribute("task", org.hamcrest.Matchers.allOf(
						org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(1L)),
						org.hamcrest.Matchers.hasProperty("category", org.hamcrest.Matchers.is("Payment")),
						org.hamcrest.Matchers.hasProperty("priority", org.hamcrest.Matchers.is("HIGH")),
						org.hamcrest.Matchers.hasProperty("status", org.hamcrest.Matchers.is("NEEDS_ATTENTION")),
						org.hamcrest.Matchers.hasProperty("aiSummary", org.hamcrest.Matchers.is("Payment issue")))));

		verify(taskService, never()).updateTask(any(Long.class), any(Task.class));
	}

	private Task existingTask() {
		Task task = new Task();
		task.setId(1L);
		task.setTitle("Payment issue");
		task.setDescription("Customers cannot pay.");
		task.setCategory("Payment");
		task.setPriority("HIGH");
		task.setStatus("NEEDS_ATTENTION");
		task.setAiSummary("Payment issue");
		task.setCreatedAt(LocalDateTime.of(2026, 9, 11, 10, 0));
		return task;
	}
}
