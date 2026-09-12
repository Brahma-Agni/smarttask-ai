package com.smarttask.smarttask_ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.smarttask.smarttask_ai.dto.TaskAiResult;
import com.smarttask.smarttask_ai.entity.Task;
import com.smarttask.smarttask_ai.repository.TaskRepository;

class TaskServiceTests {

	private final TaskRepository taskRepository = mock(TaskRepository.class);
	private final AiService aiService = mock(AiService.class);
	private final TaskService taskService = new TaskService(taskRepository, aiService);

	@Test
	void returnsTasksOrderedByPriority() {
		Task low = taskWithPriority("LOW");
		Task high = taskWithPriority("HIGH");
		Task medium = taskWithPriority("MEDIUM");
		when(taskRepository.findAll()).thenReturn(List.of(low, high, medium));

		assertEquals(List.of(high, medium, low), taskService.getAllTasks());
	}

	@Test
	void returnsDashboardCounts() {
		when(taskRepository.count()).thenReturn(12L);
		when(taskRepository.countByStatus("OPEN")).thenReturn(4L);
		when(taskRepository.countByPriority("HIGH")).thenReturn(3L);
		when(taskRepository.countByStatus("NEEDS_ATTENTION")).thenReturn(2L);
		when(taskRepository.countByStatus("COMPLETED")).thenReturn(6L);

		assertEquals(12L, taskService.countAllTasks());
		assertEquals(4L, taskService.countByStatus("OPEN"));
		assertEquals(3L, taskService.countByPriority("HIGH"));
		assertEquals(2L, taskService.countByStatus("NEEDS_ATTENTION"));
		assertEquals(6L, taskService.countByStatus("COMPLETED"));
	}

	@Test
	void createTaskAddsAiMetadataAndMarksHighPriorityAsNeedsAttention() {
		Task task = new Task();
		task.setTitle("Payment system not working");
		task.setDescription("Customers report urgent payment failures.");
		task.setCategory("Submitted category");
		task.setPriority("Submitted priority");
		task.setAiSummary("Submitted summary");
		task.setStatus("COMPLETED");
		TaskAiResult result = new TaskAiResult("Payment", "HIGH", "Payment system requires attention.");

		when(aiService.analyzeTask(task.getTitle(), task.getDescription())).thenReturn(result);
		when(taskRepository.save(task)).thenReturn(task);

		Task savedTask = taskService.createTask(task);

		verify(aiService).analyzeTask("Payment system not working", "Customers report urgent payment failures.");
		verify(taskRepository).save(task);
		assertSame(task, savedTask);
		assertEquals("Payment", savedTask.getCategory());
		assertEquals("HIGH", savedTask.getPriority());
		assertEquals("Payment system requires attention.", savedTask.getAiSummary());
		assertEquals("NEEDS_ATTENTION", savedTask.getStatus());
	}

	@Test
	void createTaskKeepsMediumPriorityOpen() {
		Task task = new Task();
		task.setStatus("COMPLETED");
		when(aiService.analyzeTask(task.getTitle(), task.getDescription()))
				.thenReturn(new TaskAiResult("General", "MEDIUM", "Review needed."));

		taskService.createTask(task);

		assertEquals("OPEN", task.getStatus());
	}

	@Test
	void createTaskKeepsLowPriorityOpen() {
		Task task = new Task();
		task.setStatus("NEEDS_ATTENTION");
		when(aiService.analyzeTask(task.getTitle(), task.getDescription()))
				.thenReturn(new TaskAiResult("General", "LOW", "Routine task."));

		taskService.createTask(task);

		assertEquals("OPEN", task.getStatus());
	}

	@Test
	void createTaskSavesFallbackResultWhenAiAnalysisIsUnavailable() {
		Task task = new Task();
		TaskAiResult fallback = new TaskAiResult("General", "MEDIUM", "AI analysis unavailable");
		when(aiService.analyzeTask(task.getTitle(), task.getDescription())).thenReturn(fallback);
		when(taskRepository.save(task)).thenReturn(task);

		Task savedTask = taskService.createTask(task);

		verify(taskRepository).save(task);
		assertEquals("General", savedTask.getCategory());
		assertEquals("MEDIUM", savedTask.getPriority());
		assertEquals("AI analysis unavailable", savedTask.getAiSummary());
		assertEquals("OPEN", savedTask.getStatus());
	}

	@Test
	void markTaskCompletedSetsCompletedStatus() {
		Task task = new Task();
		when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
		when(taskRepository.save(task)).thenReturn(task);

		Task completedTask = taskService.markTaskCompleted(1L);

		verify(taskRepository).save(task);
		assertEquals("COMPLETED", completedTask.getStatus());
	}

	private Task taskWithPriority(String priority) {
		Task task = new Task();
		task.setPriority(priority);
		return task;
	}
}
