package com.capstone.pronunciation.domain.quiz.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.quiz.dto.NextQuestionResponse;
import com.capstone.pronunciation.domain.quiz.dto.StartSessionResponse;
import com.capstone.pronunciation.domain.quiz.dto.SubmitAnswerResponse;
import com.capstone.pronunciation.domain.quiz.dto.SubmitGradedRequest;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.AnswerSubmission;
import com.capstone.pronunciation.domain.session.entity.PronunciationScore;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.AnswerSubmissionRepository;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.PronunciationScoreRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class QuizService {

	private final UserRepository userRepository;
	private final CurriculumStageRepository stageRepository;
	private final QuizQuestionRepository questionRepository;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final PronunciationScoreRepository pronunciationScoreRepository;
	private final AnswerSubmissionRepository answerSubmissionRepository;

	public QuizService(
			UserRepository userRepository,
			CurriculumStageRepository stageRepository,
			QuizQuestionRepository questionRepository,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			PronunciationScoreRepository pronunciationScoreRepository,
			AnswerSubmissionRepository answerSubmissionRepository) {
		this.userRepository = userRepository;
		this.stageRepository = stageRepository;
		this.questionRepository = questionRepository;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.pronunciationScoreRepository = pronunciationScoreRepository;
		this.answerSubmissionRepository = answerSubmissionRepository;
	}

	@Transactional
	public StartSessionResponse startSession(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Instant now = Instant.now();
		LearningSession session = learningSessionRepository.save(new LearningSession(user, now, null));
		return new StartSessionResponse(session.getId(), session.getStartTime());
	}

	@Transactional(readOnly = true)
	public NextQuestionResponse nextQuestion(String email, Long sessionId, String stageName) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		CurriculumStage stage = stageRepository.findByStageName(stageName)
				.orElseThrow(() -> new IllegalArgumentException("단계를 찾을 수 없습니다."));

		List<QuizQuestion> questions = questionRepository.findByStage_IdOrderByIdAsc(stage.getId());
		Set<Long> answered = new HashSet<>(sessionResultRepository.findQuestionIdsBySession(session.getId()));

		for (QuizQuestion q : questions) {
			if (!answered.contains(q.getId())) {
				return new NextQuestionResponse(q.getId(), stage.getStageName(), q.getSentence());
			}
		}

		return null;
	}

	@Transactional
	public SubmitAnswerResponse submitTranscript(String email, Long sessionId, Long questionId, String transcript) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		if (transcript == null || transcript.isBlank()) {
			throw new IllegalArgumentException("transcript는 필수입니다.");
		}

		String expected = q.getAnswer() != null && !q.getAnswer().isBlank() ? q.getAnswer() : q.getSentence();
		int score = scoreSimilarity(expected, transcript);

		SessionResult result = sessionResultRepository.save(new SessionResult(session, q, score));
		pronunciationScoreRepository.save(new PronunciationScore(result, score, 0));
		answerSubmissionRepository.save(new AnswerSubmission(
				result,
				transcript,
				"LOCAL",
				null,
				null,
				null,
				null,
				null,
				Instant.now()
		));

		return new SubmitAnswerResponse(result.getId(), score, expected, transcript);
	}

	@Transactional
	public SubmitAnswerResponse submitGraded(String email, Long sessionId, SubmitGradedRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
		}
		if (request.questionId() == null) {
			throw new IllegalArgumentException("questionId는 필수입니다.");
		}
		if (request.score() == null && request.voiceScore() == null && request.visionScore() == null) {
			throw new IllegalArgumentException("score 또는 voiceScore/visionScore 중 하나는 필수입니다.");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(request.questionId())
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		int voice = clampScore(request.voiceScore() != null ? request.voiceScore() : request.score());
		int vision = clampScore(request.visionScore() != null ? request.visionScore() : 0);
		int finalScore = clampScore(request.score() != null ? request.score() : voice);

		SessionResult result = sessionResultRepository.save(new SessionResult(session, q, finalScore));
		pronunciationScoreRepository.save(new PronunciationScore(result, voice, vision));
		answerSubmissionRepository.save(new AnswerSubmission(
				result,
				request.transcript(),
				"OPENAI",
				null,
				null,
				null,
				null,
				null,
				Instant.now()
		));

		String expected = q.getAnswer() != null && !q.getAnswer().isBlank() ? q.getAnswer() : q.getSentence();
		return new SubmitAnswerResponse(result.getId(), finalScore, expected, request.transcript());
	}

	@Transactional
	public SubmitAnswerResponse submitGradedAudio(
			String email,
			Long sessionId,
			Long questionId,
			String transcript,
			Integer score,
			Integer voiceScore,
			Integer visionScore,
			String audioFileName,
			String audioContentType,
			Long audioSizeBytes,
			byte[] audioData) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		LearningSession session = learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		int voice = clampScore(voiceScore != null ? voiceScore : score);
		int vision = clampScore(visionScore != null ? visionScore : 0);
		int finalScore = clampScore(score != null ? score : voice);

		SessionResult result = sessionResultRepository.save(new SessionResult(session, q, finalScore));
		pronunciationScoreRepository.save(new PronunciationScore(result, voice, vision));
		answerSubmissionRepository.save(new AnswerSubmission(
				result,
				transcript,
				"OPENAI",
				null,
				audioFileName,
				audioContentType,
				audioSizeBytes,
				audioData,
				Instant.now()
		));

		String expected = q.getAnswer() != null && !q.getAnswer().isBlank() ? q.getAnswer() : q.getSentence();
		return new SubmitAnswerResponse(result.getId(), finalScore, expected, transcript);
	}

	private static int clampScore(Integer score) {
		if (score == null) return 0;
		if (score < 0) return 0;
		if (score > 100) return 100;
		return score;
	}

	private static int scoreSimilarity(String expectedRaw, String actualRaw) {
		String expected = normalize(expectedRaw);
		String actual = normalize(actualRaw);
		if (expected.isEmpty() || actual.isEmpty()) return 0;
		if (expected.equals(actual)) return 100;

		int dist = levenshtein(expected, actual);
		int maxLen = Math.max(expected.length(), actual.length());
		double ratio = 1.0 - ((double) dist / (double) maxLen);
		int score = (int) Math.round(ratio * 100.0);
		if (score < 0) return 0;
		if (score > 100) return 100;
		return score;
	}

	private static String normalize(String s) {
		String trimmed = s == null ? "" : s.trim().toLowerCase();
		StringBuilder sb = new StringBuilder(trimmed.length());
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (Character.isLetterOrDigit(c) || Character.isSpaceChar(c)) {
				sb.append(c);
			}
		}
		return sb.toString().replaceAll("\\s+", " ").trim();
	}

	private static int levenshtein(String a, String b) {
		int n = a.length();
		int m = b.length();
		int[] prev = new int[m + 1];
		int[] curr = new int[m + 1];
		for (int j = 0; j <= m; j++) prev[j] = j;
		for (int i = 1; i <= n; i++) {
			curr[0] = i;
			char ca = a.charAt(i - 1);
			for (int j = 1; j <= m; j++) {
				int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
				curr[j] = Math.min(
						Math.min(curr[j - 1] + 1, prev[j] + 1),
						prev[j - 1] + cost
				);
			}
			int[] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		return prev[m];
	}
}
