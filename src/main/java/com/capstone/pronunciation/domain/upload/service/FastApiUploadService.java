package com.capstone.pronunciation.domain.upload.service;

import java.net.URL;
import java.net.URI;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.capstone.pronunciation.domain.upload.dto.FastApiAnalyzeRequest;
import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.FastApiLandmark;
import com.capstone.pronunciation.domain.upload.dto.FastApiRawFrame;
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
		List<FastApiRawFrame> normalizedFrames = normalizeFrames(frames);

		FastApiAnalyzeRequest request = new FastApiAnalyzeRequest(
				word,
				normalizeAudioUrl(presignedUrl.toString()),
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
				savedResult.transcript(),
				savedResult.selectedChoice(),
				savedResult.feedbackText(),
				savedResult.overallBand(),
				savedResult.phonemeFeedback(),
				savedResult.mouthComparisonAssets(),
				savedResult.llmFeedbackByMode(),
				savedResult.feedbackPayload()
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
		List<FastApiRawFrame> normalizedFrames = normalizeFrames(frames);
		FastApiAnalyzeRequest request = new FastApiAnalyzeRequest(
				word,
				normalizeAudioUrl(resolvedAudioUrl),
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
		String overallBand = readText(analysisPayload, path("overall_scores", "overall_band"));

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
				feedbackText,
				overallBand,
				firstPresentNode(
						analysisPayload,
						path("phoneme_diagnostics"),
						path("summary", "phoneme_feedback"),
						path("feedback", "phoneme_feedback")),
				firstPresentNode(
						analysisPayload,
						path("mouth_comparison_assets"),
						path("mouth_feedback"),
						path("viseme_comparison"),
						path("visual_feedback")),
				firstPresentNode(
						analysisPayload,
						path("llm_feedback_by_mode"),
						path("llm_context"),
						path("feedback_by_mode")),
				analysisPayload
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

	private List<FastApiRawFrame> normalizeFrames(List<JsonNode> frames) {
		List<FastApiRawFrame> normalizedFrames = new ArrayList<>();
		boolean hasLandmarks = false;
		for (JsonNode frame : frames) {
			if (frame == null || frame.isNull()) {
				continue;
			}

			JsonNode tMs = readNode(frame, path("t_ms"));
			if (tMs == null || tMs.isMissingNode() || tMs.isNull()) {
				tMs = readNode(frame, path("timestampMs"));
			}
			if (tMs == null || tMs.isMissingNode() || tMs.isNull()) {
				tMs = readNode(frame, path("tMs"));
			}
			if (tMs == null || tMs.isMissingNode() || tMs.isNull()) {
				tMs = readNode(frame, path("timeMs"));
			}

			JsonNode faceLandmarks = readNode(frame, path("face_landmarks"));
			if (faceLandmarks == null || faceLandmarks.isMissingNode() || faceLandmarks.isNull()) {
				faceLandmarks = readNode(frame, path("landmarks"));
			}
			if (faceLandmarks == null || faceLandmarks.isMissingNode() || faceLandmarks.isNull()) {
				faceLandmarks = readNode(frame, path("faceLandmarks"));
			}

			JsonNode faceBlendshapes = readNode(frame, path("face_blendshapes"));
			if (faceBlendshapes == null || faceBlendshapes.isMissingNode() || faceBlendshapes.isNull()) {
				faceBlendshapes = readNode(frame, path("blendshapes"));
			}
			if (faceBlendshapes == null || faceBlendshapes.isMissingNode() || faceBlendshapes.isNull()) {
				faceBlendshapes = readNode(frame, path("faceBlendshapes"));
			}
			if (faceBlendshapes == null || faceBlendshapes.isMissingNode() || faceBlendshapes.isNull()) {
				faceBlendshapes = objectMapper.createObjectNode();
			}

			List<FastApiLandmark> landmarks = normalizeLandmarks(faceLandmarks);
			Map<String, Double> blendshapeMap = normalizeBlendshapes(faceBlendshapes);
			hasLandmarks = hasLandmarks || !landmarks.isEmpty();

			normalizedFrames.add(new FastApiRawFrame(
					tMs != null && !tMs.isMissingNode() && !tMs.isNull() ? tMs.asDouble(0.0) : 0.0,
					landmarks,
					blendshapeMap
			));
		}

		if (normalizedFrames.isEmpty()) {
			throw new IllegalArgumentException("frames는 비어있지 않은 배열이어야 합니다.");
		}
		if (!hasLandmarks) {
			throw new IllegalArgumentException("frames에는 face_landmarks가 포함된 프레임이 최소 1개 이상 필요합니다.");
		}

		return normalizedFrames;
	}

	private List<FastApiLandmark> normalizeLandmarks(JsonNode faceLandmarks) {
		List<FastApiLandmark> landmarks = new ArrayList<>();
		if (faceLandmarks == null || faceLandmarks.isMissingNode() || faceLandmarks.isNull() || !faceLandmarks.isArray()) {
			return landmarks;
		}

		for (JsonNode landmark : faceLandmarks) {
			if (landmark != null && landmark.isArray()) {
				for (JsonNode nestedLandmark : landmark) {
					putLandmarkIfPresent(landmarks, nestedLandmark);
				}
				continue;
			}
			putLandmarkIfPresent(landmarks, landmark);
		}
		return landmarks;
	}

	private void putLandmarkIfPresent(List<FastApiLandmark> landmarks, JsonNode landmark) {
			if (landmark == null || landmark.isNull() || !landmark.isObject()) {
			return;
			}
			JsonNode xNode = landmark.path("x");
			JsonNode yNode = landmark.path("y");
			JsonNode zNode = landmark.path("z");
			if (!xNode.isNumber() || !yNode.isNumber() || !zNode.isNumber()) {
			return;
			}
			landmarks.add(new FastApiLandmark(
					xNode.asDouble(),
					yNode.asDouble(),
					zNode.asDouble()
			));
	}

	private Map<String, Double> normalizeBlendshapes(JsonNode faceBlendshapes) {
		Map<String, Double> blendshapes = new LinkedHashMap<>();
		if (faceBlendshapes == null || faceBlendshapes.isMissingNode() || faceBlendshapes.isNull()) {
			return blendshapes;
		}

		if (faceBlendshapes.isObject()) {
			for (Map.Entry<String, JsonNode> entry : faceBlendshapes.properties()) {
				if (entry.getValue() != null && entry.getValue().isNumber()) {
					blendshapes.put(entry.getKey(), entry.getValue().asDouble());
				}
			}
			return blendshapes;
		}

		if (!faceBlendshapes.isArray()) {
			return blendshapes;
		}

		for (JsonNode item : faceBlendshapes) {
			if (item == null || item.isNull()) {
				continue;
			}
			if (item.isArray()) {
				for (JsonNode nested : item) {
					putBlendshapeIfPresent(blendshapes, nested);
				}
				continue;
			}
			putBlendshapeIfPresent(blendshapes, item);
		}

		return blendshapes;
	}

	private void putBlendshapeIfPresent(Map<String, Double> blendshapes, JsonNode item) {
		if (item == null || item.isNull() || !item.isObject()) {
			return;
		}
		String key = readText(item, path("categoryName"), path("displayName"), path("name"));
		JsonNode scoreNode = readNode(item, path("score"));
		if (key == null || key.isBlank() || scoreNode == null || !scoreNode.isNumber()) {
			return;
		}
		blendshapes.put(key, scoreNode.asDouble());
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

	private JsonNode firstPresentNode(JsonNode root, String[]... paths) {
		for (String[] path : paths) {
			JsonNode node = readNode(root, path);
			if (node != null && !node.isMissingNode() && !node.isNull()) {
				return node;
			}
		}
		return null;
	}

	private String toJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			throw new IllegalStateException("FastAPI 응답 JSON 직렬화에 실패했습니다.", e);
		}
	}

	private String buildAnalyzeRequestJson(FastApiAnalyzeRequest request) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (Exception e) {
			throw new IllegalStateException("FastAPI 요청 JSON 직렬화에 실패했습니다.", e);
		}
	}

	private String normalizeAudioUrl(String audioUrl) {
		if (audioUrl == null || audioUrl.isBlank()) {
			throw new IllegalArgumentException("audio_url은 필수입니다.");
		}

		URI uri = URI.create(audioUrl.trim());
		if (uri.getHost() == null || uri.getHost().isBlank()) {
			throw new IllegalArgumentException("audio_url 형식이 올바르지 않습니다.");
		}
		if ("https".equalsIgnoreCase(uri.getScheme())) {
			return uri.toString();
		}
		if ("http".equalsIgnoreCase(uri.getScheme())) {
			return URI.create("https://" + uri.getRawAuthority() + uri.getRawPath()
					+ (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery())
					+ (uri.getRawFragment() == null ? "" : "#" + uri.getRawFragment()))
					.toString();
		}
		throw new IllegalArgumentException("audio_url은 HTTPS URL이어야 합니다.");
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
			String feedbackText,
			String overallBand,
			JsonNode phonemeFeedback,
			JsonNode mouthComparisonAssets,
			JsonNode llmFeedbackByMode,
			JsonNode feedbackPayload
	) {
	}

	private record HttpExchangeResult(
			int statusCode,
			String body
	) {
	}
}
