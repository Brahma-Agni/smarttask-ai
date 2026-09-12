package com.smarttask.smarttask_ai.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.smarttask.smarttask_ai.dto.TaskAiResult;

@Service
public class AiService {

	private static final Logger logger = LoggerFactory.getLogger(AiService.class);

	private static final TaskAiResult FALLBACK_RESULT =
			new TaskAiResult("General", "MEDIUM", "AI analysis unavailable");

	private final ChatClient chatClient;

	public AiService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	public TaskAiResult analyzeTask(String title, String description) {
		String prompt = """
				You are analyzing a task management item.
				Return a category, priority, and summary.
				Priority must be exactly LOW, MEDIUM, or HIGH.
				Category must be short and business-relevant.
				Summary must be one concise sentence describing the task.

				Title: %s
				Description: %s
				""".formatted(valueOrEmpty(title), valueOrEmpty(description));

		try {
			TaskAiResult result = chatClient.prompt()
					.user(prompt)
					.call()
					.entity(TaskAiResult.class, spec -> spec.useProviderStructuredOutput());

			return normalize(result);
		}
		catch (Exception exception) {
			Throwable rootCause = rootCause(exception);
			logger.warn("Gemini task analysis failed; using fallback values ({}): {}",
					rootCause.getClass().getSimpleName(), rootCause.getMessage());
			logger.debug("Gemini task analysis failure", exception);
			return FALLBACK_RESULT;
		}
	}

	private Throwable rootCause(Throwable throwable) {
		Throwable result = throwable;
		while (result.getCause() != null && result.getCause() != result) {
			result = result.getCause();
		}
		return result;
	}

	private TaskAiResult normalize(TaskAiResult result) {
		if (result == null) {
			return FALLBACK_RESULT;
		}

		String priority = valueOrEmpty(result.priority()).trim().toUpperCase(Locale.ROOT);
		if (!priority.equals("LOW") && !priority.equals("MEDIUM") && !priority.equals("HIGH")) {
			priority = "MEDIUM";
		}

		String category = valueOrEmpty(result.category()).trim();
		if (category.isBlank()) {
			category = "General";
		}

		String summary = valueOrEmpty(result.summary()).trim();
		if (summary.isBlank()) {
			summary = "AI analysis unavailable";
		}

		return new TaskAiResult(category, priority, summary);
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}
}
