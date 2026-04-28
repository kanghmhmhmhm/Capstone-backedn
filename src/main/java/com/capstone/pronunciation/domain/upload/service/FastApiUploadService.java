package com.capstone.pronunciation.domain.upload.service;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.amazonaws.services.s3.AmazonS3;
import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.upload.dto.FastApiDispatchResponse;
import com.capstone.pronunciation.domain.upload.dto.FastApiUploadRequest;
import com.capstone.pronunciation.domain.upload.entity.UploadFile;
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
import com.capstone.pronunciation.global.config.S3Config;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FastApiUploadService {

	private static final long PRESIGNED_URL_EXPIRES_IN_SECONDS = 600;

	private final RestClient restClient;
	private final AmazonS3 amazonS3;
	private final S3Config s3Config;
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
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
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
			String expectedText,
			String selectedChoice,
			List<JsonNode> frames) {
		if (sessionId == null) {
			throw new IllegalArgumentException("sessionId는 필수입니다.");
		}
		if (questionId == null) {
			throw new IllegalArgumentException("questionId는 필수입니다.");
		}
		if (expectedText == null || expectedText.isBlank()) {
			throw new IllegalArgumentException("expectedText는 필수입니다.");
		}
		if (frames == null || frames.isEmpty()) {
			throw new IllegalArgumentException("frames는 필수입니다.");
		}

		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), uploadFile.getS3Key(), expiration);

		FastApiUploadRequest request = new FastApiUploadRequest(
				expectedText,
				presignedUrl.toString(),
				frames
		);

		var responseEntity = restClient.post()
				.uri(analyzePath)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.toEntity(String.class);

		HttpStatusCode statusCode = responseEntity.getStatusCode();
		JsonNode responseBody = parseResponseBody(responseEntity.getBody());
		SavedAnalysisResult savedResult = saveAnalysisResult(uploadFile, sessionId, questionId, selectedChoice, responseBody);

		return new FastApiDispatchResponse(
				uploadFile.getId(),
				uploadFile.getObjectUrl(),
				analyzePath,
				statusCode.value(),
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
			List<JsonNode> frames) {
		if (word == null || word.isBlank()) {
			throw new IllegalArgumentException("word는 필수입니다.");
		}
		if (frames == null || frames.isEmpty()) {
			throw new IllegalArgumentException("frames는 필수입니다.");
		}

		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRES_IN_SECONDS * 1000);
		URL presignedUrl = amazonS3.generatePresignedUrl(s3Config.getBucket(), uploadFile.getS3Key(), expiration);
		FastApiUploadRequest request = new FastApiUploadRequest(
				word,
				presignedUrl.toString(),
				frames
		);

		String responseBody = restClient.post()
				.uri(analyzePath)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(String.class);

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

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, uploadFile.getUser().getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
		QuizQuestion question = quizQuestionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		double finalScore = readRequiredScore(responseBody, path("overall_scores", "fused_score_0_100"));
		double voiceScore = readRequiredScore(responseBody, path("overall_scores", "audio_score_0_100"));
		double visionScore = readRequiredScore(responseBody, path("overall_scores", "visual_score_0_100"));

		String transcript = readText(responseBody,
				path("transcript"),
				path("recognized_text"),
				path("stt_text"),
				path("word"));
		String providerPayload = toJson(responseBody);
		String feedbackText = deriveFeedbackText(responseBody);

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

	private String deriveFeedbackText(JsonNode responseBody) {
		String directFeedback = readText(responseBody,
				path("feedback"),
				path("feedback_text"),
				path("summary_text"),
				path("llm_context", "instruction"));
		if (directFeedback != null && !directFeedback.isBlank()) {
			return directFeedback;
		}

		String overallBand = readText(responseBody, path("overall_scores", "overall_band"));
		int weakestCount = readInt(responseBody, path("summary", "weakest_count"));
		int mismatchCount = readInt(responseBody, path("summary", "mismatch_count"));

		if (overallBand != null && !overallBand.isBlank()) {
			return "AI 발음 분석 완료: overall_band=%s, weakest=%d, mismatches=%d"
					.formatted(overallBand, weakestCount, mismatchCount);
		}

		return "AI 발음 분석 완료";
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

	private String toJson(JsonNode responseBody) {
		try {
			return objectMapper.writeValueAsString(responseBody);
		} catch (Exception e) {
			throw new IllegalStateException("FastAPI 응답 JSON 직렬화에 실패했습니다.", e);
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
}
