package com.capstone.pronunciation.domain.session.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.dto.SessionEndResponse;
import com.capstone.pronunciation.domain.session.dto.SessionProgressResponse;
import com.capstone.pronunciation.domain.session.dto.SessionResultItemResponse;
import com.capstone.pronunciation.domain.session.dto.SessionStartRequest;
import com.capstone.pronunciation.domain.session.dto.SessionStartResponse;
import com.capstone.pronunciation.domain.session.dto.SessionSummaryResponse;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class SessionService {

	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final UserRepository userRepository;

	public SessionService(
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			UserRepository userRepository) {
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public SessionStartResponse startSession(String email, Integer selectedLevel) {
		User user = getUser(email);
		validateSelectedLevel(selectedLevel);
		Instant now = Instant.now();
		LearningSession session = learningSessionRepository.save(new LearningSession(user, now, null, selectedLevel));
		return new SessionStartResponse(session.getId(), session.getStartTime(), session.getSelectedLevel());
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
				result.getCreatedAt()
		);
	}

	private Double averageScore(Long sessionId) {
		return sessionResultRepository.findAverageScoreBySessionId(sessionId)
				.map(avg -> Math.round(avg * 100.0) / 100.0)
				.orElse(null);
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
		if (selectedLevel < 1 || selectedLevel > 10) {
			throw new IllegalArgumentException("selectedLevel은 1~10 사이여야 합니다.");
		}
	}
}
