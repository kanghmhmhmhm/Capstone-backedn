package com.capstone.pronunciation.domain.session.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.quiz.dto.QuestionDto;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.dto.SessionEndResponse;
import com.capstone.pronunciation.domain.session.dto.SessionProgressResponse;
import com.capstone.pronunciation.domain.session.dto.SessionResumeResponse;
import com.capstone.pronunciation.domain.session.dto.SessionResultItemResponse;
import com.capstone.pronunciation.domain.session.dto.SessionStartResponse;
import com.capstone.pronunciation.domain.session.dto.SessionSummaryResponse;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionQuestion;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionQuestionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class SessionService {
	private static final String BASIC_PRONUNCIATION_STAGE = "BASIC_PRONUNCIATION";
	private static final String WORD_STAGE = "WORD";
	private static final String SENTENCE_STAGE_PREFIX = "Sentence Lv";
	private static final int SESSION_QUESTION_COUNT = 10;

	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final UserRepository userRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final SessionQuestionRepository sessionQuestionRepository;

	public SessionService(
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			UserRepository userRepository,
			QuizQuestionRepository quizQuestionRepository,
			SessionQuestionRepository sessionQuestionRepository) {
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.userRepository = userRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.sessionQuestionRepository = sessionQuestionRepository;
	}

	@Transactional
	public SessionStartResponse startSession(String email, Integer selectedLevel) {
		User user = getUser(email);
		validateSelectedLevel(selectedLevel);
		LearningSession session = resolveActiveSession(user, selectedLevel);

		List<SessionQuestion> sessionQuestions = sessionQuestionRepository.findBySession_IdOrderByQuestionOrderAsc(session.getId());
		Set<Long> solvedQuestionIds = sessionResultRepository.findQuestionIdsBySession(session.getId()).stream()
				.collect(Collectors.toSet());
		List<QuestionDto> questions = sessionQuestions.stream()
				.map(sessionQuestion -> toQuestionDto(
						sessionQuestion.getQuestion(),
						solvedQuestionIds.contains(sessionQuestion.getQuestion().getId())))
				.toList();
		Long currentQuestionId = questions.stream()
				.filter(question -> !Boolean.TRUE.equals(question.solved()))
				.map(QuestionDto::questionId)
				.findFirst()
				.orElse(null);
		boolean inProgress = !solvedQuestionIds.isEmpty() && currentQuestionId != null;

		return new SessionStartResponse(
				session.getId(),
				session.getStartTime(),
				session.getSelectedLevel(),
				inProgress,
				currentQuestionId,
				questions
		);
	}

	@Transactional
	public SessionEndResponse endSession(String email, Long sessionId) {
		LearningSession session = getOwnedSession(email, sessionId);
		if (session.getEndTime() == null) {
			session.setEndTime(Instant.now());
		}
		return new SessionEndResponse(session.getId(), session.getStartTime(), session.getEndTime(), session.getSelectedLevel());
	}

	@Transactional(readOnly = true)
	public List<SessionSummaryResponse> sessions(String email) {
		User user = getUser(email);
		return learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId()).stream()
				.map(this::toSummary)
				.toList();
	}

	@Transactional(readOnly = true)
	public SessionDetailResponse sessionDetail(String email, Long sessionId) {
		LearningSession session = getOwnedSession(email, sessionId);
		List<SessionResult> results = sessionResultRepository.findDetailedBySessionId(session.getId());
		return new SessionDetailResponse(
				session.getId(),
				session.getStartTime(),
				session.getEndTime(),
				session.getSelectedLevel(),
				results.size(),
				averageScore(session.getId()),
				results.stream().map(this::toItem).toList()
		);
	}

	@Transactional(readOnly = true)
	public SessionResumeResponse sessionResume(String email, Long sessionId) {
		LearningSession session = getOwnedSession(email, sessionId);
		List<SessionQuestion> sessionQuestions = sessionQuestionRepository.findBySession_IdOrderByQuestionOrderAsc(session.getId());
		Set<Long> solvedQuestionIds = sessionResultRepository.findQuestionIdsBySession(session.getId()).stream()
				.collect(Collectors.toSet());
		List<QuestionDto> questions = sessionQuestions.stream()
				.map(sessionQuestion -> toQuestionDto(
						sessionQuestion.getQuestion(),
						solvedQuestionIds.contains(sessionQuestion.getQuestion().getId())))
				.toList();
		Long currentQuestionId = questions.stream()
				.filter(question -> !Boolean.TRUE.equals(question.solved()))
				.map(QuestionDto::questionId)
				.findFirst()
				.orElse(null);
		List<SessionResultItemResponse> submittedResults = sessionResultRepository.findDetailedBySessionId(session.getId()).stream()
				.map(this::toItem)
				.toList();

		return new SessionResumeResponse(
				session.getId(),
				session.getStartTime(),
				session.getEndTime(),
				session.getSelectedLevel(),
				session.getEndTime() == null && currentQuestionId != null,
				currentQuestionId,
				submittedResults.size(),
				questions,
				submittedResults
		);
	}

	@Transactional(readOnly = true)
	public SessionProgressResponse sessionProgress(String email, Long sessionId) {
		LearningSession session = getOwnedSession(email, sessionId);
		int answeredQuestions = Math.toIntExact(sessionResultRepository.countBySession_Id(session.getId()));
		Instant lastAnsweredAt = sessionResultRepository.findTopBySession_IdOrderByCreatedAtDescIdDesc(session.getId())
				.map(SessionResult::getCreatedAt)
				.orElse(null);

		return new SessionProgressResponse(
				session.getId(),
				session.getSelectedLevel(),
				answeredQuestions,
				averageScore(session.getId()),
				session.getStartTime(),
				session.getEndTime(),
				lastAnsweredAt,
				session.getEndTime() != null
		);
	}

	private SessionSummaryResponse toSummary(LearningSession session) {
		int totalAnswers = Math.toIntExact(sessionResultRepository.countBySession_Id(session.getId()));
		Instant lastAnsweredAt = sessionResultRepository.findTopBySession_IdOrderByCreatedAtDescIdDesc(session.getId())
				.map(SessionResult::getCreatedAt)
				.orElse(null);
		return new SessionSummaryResponse(
				session.getId(),
				session.getStartTime(),
				session.getEndTime(),
				session.getSelectedLevel(),
				totalAnswers,
				averageScore(session.getId()),
				lastAnsweredAt
		);
	}

	private SessionResultItemResponse toItem(SessionResult result) {
		return new SessionResultItemResponse(
				result.getId(),
				result.getQuestion().getId(),
				result.getQuestion().getStage().getStageName(),
				result.getQuestion().getSentence(),
				result.getScore(),
				result.getPronunciationScore() == null ? null : result.getPronunciationScore().getVoiceScore(),
				result.getPronunciationScore() == null ? null : result.getPronunciationScore().getVisionScore(),
				result.getSubmission() == null ? null : result.getSubmission().getTranscript(),
				result.getSubmission() == null ? null : result.getSubmission().getSelectedChoice(),
				result.getSubmission() == null || result.getSubmission().getUploadFile() == null ? null : result.getSubmission().getUploadFile().getId(),
				result.getSubmission() == null || result.getSubmission().getUploadFile() == null ? null : result.getSubmission().getUploadFile().getObjectUrl(),
				result.getCreatedAt()
		);
	}

	private Double averageScore(Long sessionId) {
		return sessionResultRepository.findAverageScoreBySessionId(sessionId)
				.map(avg -> Math.round(avg * 10.0) / 10.0)
				.orElse(null);
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private List<QuizQuestion> pickSentenceQuestionsForLevel(Integer selectedLevel, int count) {
		if (selectedLevel == null) {
			throw new IllegalArgumentException("selectedLevel은 필수입니다.");
		}

		List<QuizQuestion> candidates = resolveQuestionsForLevel(selectedLevel);

		if (candidates.isEmpty()) {
			return List.of();
		}

		Collections.shuffle(candidates);
		return candidates.stream()
				.limit(count)
				.toList();
	}

	private List<QuizQuestion> resolveQuestionsForLevel(Integer selectedLevel) {
		if (selectedLevel == 1) {
			return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc(BASIC_PRONUNCIATION_STAGE);
		}
		if (selectedLevel == 2) {
			return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc(WORD_STAGE);
		}
		return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc(sentenceStageName(selectedLevel));
	}

	private QuestionDto toQuestionDto(QuizQuestion question, boolean solved) {
		String answer = question.getAnswer() != null && !question.getAnswer().isBlank()
				? question.getAnswer()
				: question.getSentence();
		return new QuestionDto(
				question.getId(),
				question.getStage().getStageName(),
				question.getDifficulty(),
				question.getSentence(),
				answer,
				question.getChoiceOptions(),
				question.getAnimationData(),
				solved
		);
	}

	private LearningSession createSessionWithQuestions(User user, Integer selectedLevel) {
		Instant now = Instant.now();
		LearningSession session = learningSessionRepository.save(new LearningSession(user, now, null, selectedLevel));
		List<QuizQuestion> questions = pickSentenceQuestionsForLevel(selectedLevel, SESSION_QUESTION_COUNT);
		for (int i = 0; i < questions.size(); i++) {
			sessionQuestionRepository.save(new SessionQuestion(session, questions.get(i), i + 1));
		}
		return session;
	}

	private LearningSession resolveActiveSession(User user, Integer selectedLevel) {
		List<LearningSession> openSessions = learningSessionRepository
				.findByUser_IdAndSelectedLevelAndEndTimeIsNullOrderByStartTimeDesc(user.getId(), selectedLevel);

		if (openSessions.isEmpty()) {
			return createSessionWithQuestions(user, selectedLevel);
		}

		LearningSession reusableSession = null;
		for (LearningSession candidate : openSessions) {
			boolean hasAssignedQuestions = !sessionQuestionRepository
					.findBySession_IdOrderByQuestionOrderAsc(candidate.getId())
					.isEmpty();
			if (hasAssignedQuestions) {
				reusableSession = candidate;
				break;
			}
		}

		if (reusableSession == null) {
			closeOpenSessions(openSessions);
			return createSessionWithQuestions(user, selectedLevel);
		}

		closeOtherOpenSessions(openSessions, reusableSession.getId());
		return reusableSession;
	}

	private void closeOpenSessions(List<LearningSession> sessions) {
		Instant now = Instant.now();
		for (LearningSession session : sessions) {
			session.setEndTime(now);
		}
	}

	private void closeOtherOpenSessions(List<LearningSession> sessions, Long keepSessionId) {
		Instant now = Instant.now();
		for (LearningSession session : sessions) {
			if (!session.getId().equals(keepSessionId)) {
				session.setEndTime(now);
			}
		}
	}

	private LearningSession getOwnedSession(String email, Long sessionId) {
		User user = getUser(email);
		return learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
	}

	private void validateSelectedLevel(Integer selectedLevel) {
		if (selectedLevel == null) {
			throw new IllegalArgumentException("selectedLevel은 필수입니다.");
		}
		if (selectedLevel < 1 || selectedLevel > 15) {
			throw new IllegalArgumentException("selectedLevel은 1~15 사이여야 합니다.");
		}
	}

	private static String sentenceStageName(Integer selectedLevel) {
		return SENTENCE_STAGE_PREFIX + selectedLevel;
	}
}
