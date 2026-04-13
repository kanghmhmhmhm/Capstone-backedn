package com.capstone.pronunciation.domain.feedback.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.feedback.dto.FeedbackItemResponse;
import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class FeedbackService {

	private final FeedbackLogRepository feedbackLogRepository;
	private final UserRepository userRepository;
	private final LearningSessionRepository learningSessionRepository;

	public FeedbackService(
			FeedbackLogRepository feedbackLogRepository,
			UserRepository userRepository,
			LearningSessionRepository learningSessionRepository) {
		this.feedbackLogRepository = feedbackLogRepository;
		this.userRepository = userRepository;
		this.learningSessionRepository = learningSessionRepository;
	}

	@Transactional(readOnly = true)
	public List<FeedbackItemResponse> recent(String email, int limit) {
		validateLimit(limit);
		User user = getUser(email);
		return feedbackLogRepository.findByUserId(user.getId())
				.stream()
				.limit(limit)
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FeedbackItemResponse> bySession(String email, Long sessionId) {
		User user = getUser(email);
		learningSessionRepository.findByIdAndUser_Id(sessionId, user.getId())
				.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));

		return feedbackLogRepository.findByUserIdAndSessionId(user.getId(), sessionId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private FeedbackItemResponse toResponse(FeedbackLog log) {
		return new FeedbackItemResponse(
				log.getId(),
				log.getResult().getSession().getId(),
				log.getResult().getId(),
				log.getResult().getQuestion().getId(),
				log.getResult().getQuestion().getSentence(),
				log.getResult().getScore(),
				log.getMode(),
				log.getFeedbackText()
		);
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private void validateLimit(int limit) {
		if (limit <= 0 || limit > 100) {
			throw new IllegalArgumentException("limit은 1~100 사이여야 합니다.");
		}
	}
}
