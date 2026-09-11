package com.smarttask.smarttask_ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.smarttask.smarttask_ai.service.TaskService;

@Controller
public class DashboardController {

	private final TaskService taskService;

	public DashboardController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping("/")
	public String dashboard(Model model) {
		model.addAttribute("totalTasks", taskService.countAllTasks());
		model.addAttribute("openTasks", taskService.countByStatus("OPEN"));
		model.addAttribute("highPriorityTasks", taskService.countByPriority("HIGH"));
		model.addAttribute("needsAttentionTasks", taskService.countByStatus("NEEDS_ATTENTION"));
		model.addAttribute("completedTasks", taskService.countByStatus("COMPLETED"));

		return "dashboard";
	}
}
