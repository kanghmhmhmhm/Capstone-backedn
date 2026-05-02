package com.capstone.pronunciation.domain.upload.service;

import java.net.URL;
import java.net.URI;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.AnswerSubmission;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.PronunciationScore;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.AnswerSubmissionRepository;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.PronunciationScoreRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.FastApiUploadRequest;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
import com.capstone.pronunciation.global.config.S3Config;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FastApiUploadService {

	private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 600;
	private static final Logger log = LoggerFactory.getLogger(FastApiUploadService.class);

	private final AmazonS3 amazonS3;
	private final S3Config s3Config;
	private final String baseUrl;
	private final String analyzePath;
	private final LearningSessionRepository learningSessionRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final PronunciationScoreRepository pronunciationScoreRepository;
	private final AnswerSubmissionRepository answerSubmissionRepository;
	private final FeedbackLogRepository feedbackLogRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public FastApiUploadService(
			@Value("${app.fastapi.base-url:http://localhost:8000}") String baseUrl,
			@Value("${app.fastapi.analyze-path:/analyze}") String analyzePath,
			AmazonS3 amazonS3,
			S3Config s3Config,
			LearningSessionRepository learningSessionRepository,
			QuizQuestionRepository quizQuestionRepository,
			SessionResultRepository sessionResultRepository,
			PronunciationScoreRepository pronunciationScoreRepository,
			AnswerSubmissionRepository answerSubmissionRepository,
			FeedbackLogRepository feedbackLogRepository) {
		this.baseUrl = baseUrl;
		this.amazonS3 = amazonS3;
		this.s3Config = s3Config;
		this.analyzePath = analyzePath;
		this.learningSessionRepository = learningSessionRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.pronunciationScoreRepository = pronunciationScoreRepository;
		this.answerSubmissionRepository = answerSubmissionRepository;
		this.feedbackLogRepository = feedbackLogRepository;
	}

	@Transactional
	public FastApiDispatchResponse sendUpload(
			UploadFile uploadFile,
			Long sessionId,
			Long questionId,
			String word,
			String selectedChoice,
			List<JsonNode> frames) {
		if (sessionId == null) {
			throw new IllegalArgumentException("sessionId는 필수입니다.");
		}
		if (questionId == null) {
			throw new IllegalArgumentException("questionId는 필수입니다.");
		}
		if (word == null || word.isBlank()) {
			throw new IllegalArgumentException("word는 필수입니다.");
		}
		if (frames == null || frames.isEmpty()) {
			throw new IllegalArgumentException("frames는 필수입니다.");
		}

		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), uploadFile.getS3Key(), expiration);
		List<JsonNode> normalizedFrames = normalizeFrames(frames);

		FastApiUploadRequest request = new FastApiUploadRequest(
				word,
				presignedUrl.toString(),
				normalizedFrames
		);
		String requestJson = buildAnalyzeRequestJson(request);
		logOutgoingRequest("sendUpload", requestJson);

		HttpExchangeResult responseEntity = executeAnalyzeRequest(requestJson);
		logIncomingResponse("sendUpload", HttpStatusCode.valueOf(responseEntity.statusCode()), responseEntity.body());

		HttpStatusCode statusCode = HttpStatusCode.valueOf(responseEntity.statusCode());
		JsonNode responseBody = parseResponseBody(responseEntity.body());
		SavedAnalysisResult savedResult = saveAnalysisResult(uploadFile, sessionId, questionId, selectedChoice, responseBody);

		return new FastApiDispatchResponse(
				uploadFile.getId(),
				uploadFile.getObjectUrl(),
				analyzePath,
				responseEntity.statusCode(),
				savedResult.resultId(),
				savedResult.score(),
				savedResult.voiceScore(),
				savedResult.visionScore(),
				savedResult.transcript(),
				savedResult.selectedChoice(),
				savedResult.feedbackText()
		);
	}

	@Transactional(readOnly = true)
	public JsonNode testAnalyze(
			UploadFile uploadFile,
			String word,
			String audioUrl,
			List<JsonNode> frames) {
		if (word == null || word.isBlank()) {
			throw new IllegalArgumentException("word는 필수입니다.");
		}
		if (frames == null || frames.isEmpty()) {
			throw new IllegalArgumentException("frames는 필수입니다.");
		}

		String resolvedAudioUrl = audioUrl;
		if (resolvedAudioUrl == null || resolvedAudioUrl.isBlank()) {
			Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
			URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), uploadFile.getS3Key(), expiration);
			resolvedAudioUrl = presignedUrl.toString();
		}
		List<JsonNode> normalizedFrames = normalizeFrames(frames);
		FastApiUploadRequest request = new FastApiUploadRequest(
				word,
				resolvedAudioUrl,
				normalizedFrames
		);
		String requestJson = buildAnalyzeRequestJson(request);
		logOutgoingRequest("testAnalyze", requestJson);

		HttpExchangeResult responseEntity = executeAnalyzeRequest(requestJson);
		String responseBody = responseEntity.body();
		logIncomingResponse("testAnalyze", null, responseBody);

		return parseResponseBody(responseBody);
	}

	private SavedAnalysisResult saveAnalysisResult(
			UploadFile uploadFile,
			Long sessionId,
			Long questionId,
			String selectedChoice,
			JsonNode responseBody) {
		if (responseBody == null || responseBody.isNull()) {
			throw new IllegalStateException("FastAPI 응답 본문이 비어 있습니다.");
		}
		JsonNode analysisPayload = extractAnalysisPayload(responseBody);

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, uploadFile.getUser().getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
		QuizQuestion question = quizQuestionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		double finalScore = readRequiredScore(analysisPayload, path("overall_scores", "fused_score_0_100"));
		double voiceScore = readRequiredScore(analysisPayload, path("overall_scores", "audio_score_0_100"));
		double visionScore = readRequiredScore(analysisPayload, path("overall_scores", "visual_score_0_100"));

		String transcript = readText(responseBody,
				path("transcript"),
				path("recognized_text"),
				path("stt_text"),
				path("word"));
		if (transcript == null || transcript.isBlank()) {
			transcript = readText(analysisPayload, path("word"));
		}
		String providerPayload = toJson(responseBody);
		String feedbackText = deriveFeedbackText(analysisPayload, responseBody);

		SessionResult result = sessionResultRepository.save(new SessionResult(session, question, finalScore));
		pronunciationScoreRepository.save(new PronunciationScore(result, voiceScore, visionScore));
		answerSubmissionRepository.save(new AnswerSubmission(
				result,
				transcript,
				selectedChoice,
				"FASTAPI",
				providerPayload,
				uploadFile,
				Instant.now()
		));

		if (!feedbackText.isBlank()) {
			feedbackLogRepository.save(new FeedbackLog(result, "FASTAPI", feedbackText));
		}

		return new SavedAnalysisResult(
				result.getId(),
				finalScore,
				voiceScore,
				visionScore,
				transcript,
				selectedChoice,
				feedbackText
		);
	}

	private String deriveFeedbackText(JsonNode analysisPayload, JsonNode responseBody) {
		String directFeedback = readText(responseBody,
				path("feedback"),
				path("feedback_text"),
				path("summary_text"),
				path("llm_context", "instruction"));
		if (directFeedback != null && !directFeedback.isBlank()) {
			return directFeedback;
		}
		directFeedback = readText(analysisPayload,
				path("feedback"),
				path("feedback_text"),
				path("summary_text"));
		if (directFeedback != null && !directFeedback.isBlank()) {
			return directFeedback;
		}

		String overallBand = readText(analysisPayload, path("overall_scores", "overall_band"));
		int weakCount = readInt(analysisPayload, path("summary", "weak_count"));
		int mismatchCount = readInt(analysisPayload, path("summary", "mismatch_count"));
		String praisePoint = readText(analysisPayload, path("llm_context", "praise_point"));
		String keyIssuePhoneme = readText(analysisPayload, path("llm_context", "key_issues", "0", "phoneme"));
		String howToFix = readText(analysisPayload, path("llm_context", "key_issues", "0", "how_to_fix"));

		StringBuilder summary = new StringBuilder("AI 발음 분석 완료");
		if (overallBand != null && !overallBand.isBlank()) {
			summary.append(": overall_band=").append(overallBand);
		}
		summary.append(", weak=").append(weakCount)
				.append(", mismatches=").append(mismatchCount);
		if (praisePoint != null && !praisePoint.isBlank()) {
			summary.append(". ").append(praisePoint);
		}
		if (keyIssuePhoneme != null && !keyIssuePhoneme.isBlank()) {
			summary.append(" 핵심 교정 음소는 /").append(keyIssuePhoneme).append("/ 입니다.");
		}
		if (howToFix != null && !howToFix.isBlank()) {
			summary.append(" ").append(howToFix);
		}

		return summary.toString();
	}

	private JsonNode extractAnalysisPayload(JsonNode responseBody) {
		JsonNode nestedPayload = readNode(responseBody, path("feedback_payload"));
		if (nestedPayload != null && !nestedPayload.isMissingNode() && !nestedPayload.isNull()) {
			return nestedPayload;
		}
		return responseBody;
	}

	private List<JsonNode> normalizeFrames(List<JsonNode> frames) {
		List<JsonNode> normalizedFrames = new ArrayList<>();
		for (JsonNode frame : frames) {
			if (frame == null || frame.isNull()) {
				continue;
			}

			JsonNode tMs = readNode(frame, path("t_ms"));
			if (tMs == null || tMs.isMissingNode() || tMs.isNull()) {
				tMs = readNode(frame, path("timestampMs"));
			}

			JsonNode faceLandmarks = readNode(frame, path("face_landmarks"));
			if (faceLandmarks == null || faceLandmarks.isMissingNode() || faceLandmarks.isNull()) {
				faceLandmarks = readNode(frame, path("landmarks"));
			}

			JsonNode faceBlendshapes = readNode(frame, path("face_blendshapes"));
			if (faceBlendshapes == null || faceBlendshapes.isMissingNode() || faceBlendshapes.isNull()) {
				faceBlendshapes = objectMapper.createObjectNode();
			}
			if (faceLandmarks == null || faceLandmarks.isMissingNode() || faceLandmarks.isNull()) {
				faceLandmarks = objectMapper.createArrayNode();
			}

			var normalized = objectMapper.createObjectNode();
			normalized.set("t_ms", tMs != null && !tMs.isMissingNode() && !tMs.isNull()
					? tMs
					: objectMapper.getNodeFactory().numberNode(0));
			normalized.set("face_landmarks", faceLandmarks);
			normalized.set("face_blendshapes", faceBlendshapes);
			normalizedFrames.add(normalized);
		}

		if (normalizedFrames.isEmpty()) {
			throw new IllegalArgumentException("frames는 비어있지 않은 배열이어야 합니다.");
		}

		return normalizedFrames;
	}

	private double readRequiredScore(JsonNode root, String[] path) {
		JsonNode node = readNode(root, path);
		if (node == null || node.isMissingNode() || node.isNull()) {
			throw new IllegalStateException("FastAPI 응답에 필수 점수 필드가 없습니다: " + String.join(".", path));
		}
		double value = node.asDouble(Double.NaN);
		if (Double.isNaN(value)) {
			throw new IllegalStateException("FastAPI 점수 값이 숫자가 아닙니다: " + String.join(".", path));
		}
		return clampScore(value);
	}

	private int readInt(JsonNode root, String[] path) {
		JsonNode node = readNode(root, path);
		if (node == null || node.isMissingNode() || node.isNull()) {
			return 0;
		}
		return node.asInt(0);
	}

	private String readText(JsonNode root, String[]... paths) {
		for (String[] path : paths) {
			JsonNode node = readNode(root, path);
			if (node == null || node.isMissingNode() || node.isNull()) {
				continue;
			}
			String value = node.asText(null);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private JsonNode readNode(JsonNode root, String[] path) {
		JsonNode current = root;
		for (String part : path) {
			if (current == null || current.isMissingNode() || current.isNull()) {
				return null;
			}
			current = current.path(part);
		}
		return current;
	}

	private String toJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			throw new IllegalStateException("FastAPI 응답 JSON 직렬화에 실패했습니다.", e);
		}
	}

	private String buildAnalyzeRequestJson(FastApiUploadRequest request) {
		try {
			var payload = objectMapper.createObjectNode();
			payload.put("word", request.word());
			payload.put("audio_url", request.audioUrl());
			payload.set("frames", objectMapper.valueToTree(request.frames()));
			return objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			throw new IllegalStateException("FastAPI 요청 JSON 직렬화에 실패했습니다.", e);
		}
	}

	private static String[] path(String... values) {
		return values;
	}

	private JsonNode parseResponseBody(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			throw new IllegalStateException("AI 서버 응답 본문이 비어 있습니다.");
		}

		try {
			return objectMapper.readTree(responseBody);
		} catch (Exception e) {
			throw new IllegalStateException("AI 서버 응답을 JSON으로 해석할 수 없습니다: " + responseBody, e);
		}
	}

	private static double clampScore(double value) {
		double bounded = Math.max(0.0, Math.min(100.0, value));
		return Math.round(bounded * 10.0) / 10.0;
	}

	private HttpExchangeResult executeAnalyzeRequest(String requestJson) {
		URI analyzeUri = buildAnalyzeUri();
		HttpURLConnection connection = null;
		try {
			byte[] requestBytes = requestJson.getBytes(StandardCharsets.UTF_8);
			log.info("FastAPI analyze target: uri={}, payloadLength={}", analyzeUri, requestJson == null ? 0 : requestJson.length());
			connection = (HttpURLConnection) analyzeUri.toURL().openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
			connection.setFixedLengthStreamingMode(requestBytes.length);

			try (var outputStream = connection.getOutputStream()) {
				outputStream.write(requestBytes);
			}

			int statusCode = connection.getResponseCode();
			String responseBody = readResponseBody(connection, statusCode);

			if (statusCode >= 400) {
				throw new HttpClientErrorException(
						HttpStatusCode.valueOf(statusCode),
						connection.getResponseMessage(),
						null,
						responseBody == null ? new byte[0] : responseBody.getBytes(StandardCharsets.UTF_8),
						StandardCharsets.UTF_8
				);
			}

			return new HttpExchangeResult(statusCode, responseBody);
		} catch (RestClientResponseException e) {
			log.warn(
					"FastAPI analyze request failed: uri={}, status={}, body={}",
					analyzeUri,
					e.getStatusCode(),
					e.getResponseBodyAsString()
			);
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("AI 서버 HTTP 요청에 실패했습니다.", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
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

	private URI buildAnalyzeUri() {
		String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		String normalizedPath = analyzePath.startsWith("/") ? analyzePath : "/" + analyzePath;
		return URI.create(normalizedBaseUrl + normalizedPath);
	}

	private void logOutgoingRequest(String source, String requestJson) {
		log.info("FastAPI {} request payload: {}", source, requestJson);
	}

	private void logIncomingResponse(String source, HttpStatusCode statusCode, String responseBody) {
		if (statusCode != null) {
			log.info("FastAPI {} response status: {}", source, statusCode.value());
		}
		log.info("FastAPI {} response body: {}", source, responseBody);
	}

	private record SavedAnalysisResult(
			Long resultId,
			Double score,
			Double voiceScore,
			Double visionScore,
			String transcript,
			String selectedChoice,
			String feedbackText
	) {
	}

	private record HttpExchangeResult(
			int statusCode,
			String body
	) {
	}
}
