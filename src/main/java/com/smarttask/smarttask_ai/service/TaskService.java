package com.smarttask.smarttask_ai.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smarttask.smarttask_ai.dto.TaskAiResult;
import com.smarttask.smarttask_ai.entity.Task;
import com.smarttask.smarttask_ai.repository.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final AiService aiService;

	public TaskService(TaskRepository taskRepository, AiService aiService) {
		this.taskRepository = taskRepository;
		this.aiService = aiService;
	}

	public List<Task> getAllTasks() {
		return taskRepository.findAll().stream()
				.sorted(Comparator.comparingInt(this::priorityRank))
				.toList();
	}

	private int priorityRank(Task task) {
		if ("HIGH".equalsIgnoreCase(task.getPriority())) {
			return 0;
		}
		if ("MEDIUM".equalsIgnoreCase(task.getPriority())) {
			return 1;
		}
		if ("LOW".equalsIgnoreCase(task.getPriority())) {
			return 2;
		}
		return 3;
	}

	public Task getTaskById(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
	}

	public long countAllTasks() {
		return taskRepository.count();
	}

	public long countByStatus(String status) {
		return taskRepository.countByStatus(status);
	}

	public long countByPriority(String priority) {
		return taskRepository.countByPriority(priority);
	}

	public Task createTask(Task task) {
		TaskAiResult result = aiService.analyzeTask(task.getTitle(), task.getDescription());
		task.setCategory(result.category());
		task.setPriority(result.priority());
		task.setAiSummary(result.summary());
		task.setStatus("HIGH".equalsIgnoreCase(result.priority()) ? "NEEDS_ATTENTION" : "OPEN");

		return taskRepository.save(task);
	}

	public Task updateTask(Long id, Task task) {
		Task existingTask = getTaskById(id);
		existingTask.setTitle(task.getTitle());
		existingTask.setDescription(task.getDescription());
		existingTask.setCategory(task.getCategory());
		existingTask.setPriority(task.getPriority());
		existingTask.setStatus(task.getStatus());
		existingTask.setAiSummary(task.getAiSummary());

		return taskRepository.save(existingTask);
	}

	public void deleteTask(Long id) {
		Task task = getTaskById(id);
		taskRepository.delete(task);
	}

	public Task markTaskCompleted(Long id) {
		Task task = getTaskById(id);
		task.setStatus("COMPLETED");

		return taskRepository.save(task);
	}
}
