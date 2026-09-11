package com.smarttask.smarttask_ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import com.smarttask.smarttask_ai.dto.TaskAiResult;

class AiServiceTests {

	private final ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
	private final ChatClient chatClient = mock(ChatClient.class);
	private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
	private final ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
	private final AiService aiService;

	AiServiceTests() {
		when(chatClientBuilder.build()).thenReturn(chatClient);
		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		aiService = new AiService(chatClientBuilder);
	}

	@Test
	void returnsNormalizedStructuredResult() {
		when(responseSpec.entity(eq(TaskAiResult.class), any()))
				.thenReturn(new TaskAiResult(" Payment ", "high", " Payment processing is unavailable. "));

		TaskAiResult result = aiService.analyzeTask(
				"Payment system not working",
				"Customers cannot complete payments.");

		assertEquals("Payment", result.category());
		assertEquals("HIGH", result.priority());
		assertEquals("Payment processing is unavailable.", result.summary());

		ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
		verify(requestSpec).user(promptCaptor.capture());
		assertTrue(promptCaptor.getValue().contains("Payment system not working"));
		assertTrue(promptCaptor.getValue().contains("Customers cannot complete payments."));
	}

	@Test
	void replacesInvalidOrBlankValuesWithDefaults() {
		when(responseSpec.entity(eq(TaskAiResult.class), any()))
				.thenReturn(new TaskAiResult(" ", "immediate", null));

		TaskAiResult result = aiService.analyzeTask("Task", "Description");

		assertEquals("General", result.category());
		assertEquals("MEDIUM", result.priority());
		assertEquals("AI analysis unavailable", result.summary());
	}

	@Test
	void returnsFallbackWhenAiCallFails() {
		when(responseSpec.entity(eq(TaskAiResult.class), any()))
				.thenThrow(new RuntimeException("Gemini unavailable"));

		TaskAiResult result = aiService.analyzeTask("Task", "Description");

		assertEquals("General", result.category());
		assertEquals("MEDIUM", result.priority());
		assertEquals("AI analysis unavailable", result.summary());
	}
}
