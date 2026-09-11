package com.smarttask.smarttask_ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smarttask.smarttask_ai.entity.Task;
import com.smarttask.smarttask_ai.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping
	public String listTasks(Model model) {
		model.addAttribute("tasks", taskService.getAllTasks());
		return "task-list";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("task", new Task());
		return "task-form";
	}

	@PostMapping
	public String createTask(@Valid @ModelAttribute("task") Task task, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "task-form";
		}

		taskService.createTask(task);
		return "redirect:/tasks";
	}

	@GetMapping("/{id}")
	public String viewTask(@PathVariable Long id, Model model) {
		model.addAttribute("task", taskService.getTaskById(id));
		return "task-details";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model) {
		model.addAttribute("task", taskService.getTaskById(id));
		return "task-form";
	}

	@PostMapping("/{id}")
	public String updateTask(@PathVariable Long id, @Valid @ModelAttribute("task") Task task,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			Task existingTask = taskService.getTaskById(id);
			task.setId(existingTask.getId());
			task.setCategory(existingTask.getCategory());
			task.setPriority(existingTask.getPriority());
			task.setStatus(existingTask.getStatus());
			task.setAiSummary(existingTask.getAiSummary());
			task.setCreatedAt(existingTask.getCreatedAt());
			return "task-form";
		}

		taskService.updateTask(id, task);
		return "redirect:/tasks/" + id;
	}

	@PostMapping("/{id}/delete")
	public String deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return "redirect:/tasks";
	}

	@PostMapping("/{id}/complete")
	public String markTaskCompleted(@PathVariable Long id) {
		taskService.markTaskCompleted(id);
		return "redirect:/tasks/" + id;
	}
}
