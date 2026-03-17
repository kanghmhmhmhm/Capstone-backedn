package com.capstone.pronunciation.domain.history.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.history.dto.StudyHistoryItemResponse;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class HistoryService {
	private final SessionResultRepository sessionResultRepository;
	private final UserRepository userRepository;

	public HistoryService(SessionResultRepository sessionResultRepository, UserRepository userRepository) {
		this.sessionResultRepository = sessionResultRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<StudyHistoryItemResponse> recentResults(String email, int limit) {
		if (limit <= 0 || limit > 200) {
			throw new IllegalArgumentException("limit은 1~200 사이여야 합니다.");
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		List<SessionResult> results = sessionResultRepository.findRecentByUserId(user.getId(), limit);
		return results.stream().map(r -> new StudyHistoryItemResponse(
				r.getId(),
				r.getSession().getId(),
				r.getSession().getStartTime(),
				r.getSession().getEndTime(),
				r.getQuestion().getStage().getStageName(),
				r.getQuestion().getId(),
				r.getQuestion().getSentence(),
				r.getScore(),
				r.getPronunciationScore() == null ? null : r.getPronunciationScore().getVoiceScore(),
				r.getPronunciationScore() == null ? null : r.getPronunciationScore().getVisionScore(),
				r.getSubmission() == null ? null : r.getSubmission().getTranscript(),
				r.getCreatedAt()
		)).toList();
	}
}

