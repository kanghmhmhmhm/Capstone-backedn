package com.capstone.pronunciation.domain.upload.service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class FeedbackWavService {
	private static final Logger log = LoggerFactory.getLogger(FeedbackWavService.class);

	private final String baseUrl;
	private final String feedbackWavPath;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public FeedbackWavService(
			@Value("${app.fastapi.base-url:http://localhost:8000}") String baseUrl,
			@Value("${app.fastapi.feedback-wav-path:/feedback-wav}") String feedbackWavPath) {
		this.baseUrl = baseUrl;
		this.feedbackWavPath = feedbackWavPath;
	}

	public FeedbackWavResult synthesize(JsonNode request) {
		String analysisText = validateAndExtractAnalysisText(request);

		URI feedbackWavUri = buildFeedbackWavUri();
		HttpURLConnection connection = null;
		try {
			log.info("FastAPI feedback-wav target: uri={}", feedbackWavUri);
			connection = (HttpURLConnection) feedbackWavUri.toURL().openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);

			try (var outputStream = connection.getOutputStream()) {
				objectMapper.writeValue(outputStream, toFastApiRequestBody(analysisText));
			}

			int statusCode = connection.getResponseCode();
			String responseBody = readResponseBody(connection, statusCode);
			log.info("FastAPI feedback-wav response status: {}", statusCode);
			return new FeedbackWavResult(statusCode, parseJsonOrWrap(responseBody, statusCode));
		} catch (Exception e) {
			throw new IllegalStateException("AI 서버 feedback-wav 요청에 실패했습니다.", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String validateAndExtractAnalysisText(JsonNode request) {
		if (request == null) {
			throw new IllegalArgumentException("요청 본문은 필수입니다.");
		}
		String analysisText = null;
		JsonNode snakeCase = request.path("analysis_text");
		if (!snakeCase.isMissingNode() && !snakeCase.isNull()) {
			analysisText = snakeCase.asText(null);
		}
		if (analysisText == null || analysisText.isBlank()) {
			JsonNode camelCase = request.path("analysisText");
			if (!camelCase.isMissingNode() && !camelCase.isNull()) {
				analysisText = camelCase.asText(null);
			}
		}
		if (analysisText == null || analysisText.isBlank()) {
			throw new IllegalArgumentException("analysis_text는 필수입니다.");
		}
		return analysisText;
	}

	private ObjectNode toFastApiRequestBody(String analysisText) {
		ObjectNode body = objectMapper.createObjectNode();
		body.put("analysis_text", analysisText);
		return body;
	}

	private JsonNode parseJsonOrWrap(String responseBody, int statusCode) {
		if (responseBody == null || responseBody.isBlank()) {
			ObjectNode body = objectMapper.createObjectNode();
			body.put("detail", "AI 서버 feedback-wav 응답 본문이 비어 있습니다.");
			body.put("status", statusCode);
			return body;
		}
		try {
			return objectMapper.readTree(responseBody);
		} catch (Exception e) {
			ObjectNode body = objectMapper.createObjectNode();
			body.put("detail", responseBody);
			body.put("status", statusCode);
			return body;
		}
	}

	private String readResponseBody(HttpURLConnection connection, int statusCode) throws Exception {
		var inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
		if (inputStream == null) {
			return "";
		}
		try (inputStream) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private URI buildFeedbackWavUri() {
		String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		String normalizedPath = feedbackWavPath.startsWith("/") ? feedbackWavPath : "/" + feedbackWavPath;
		return URI.create(normalizedBaseUrl + normalizedPath);
	}

	public record FeedbackWavResult(
			int statusCode,
			JsonNode body
	) {
	}
}
