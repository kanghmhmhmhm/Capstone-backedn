package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;
import java.util.List;

public record SessionDetailResponse(
		Long sessionId,
		Instant startTime,
		Instant endTime,
		Integer selectedLevel,
		int totalAnswers,
		Double averageScore,
		List<SessionResultItemResponse> results
) {
}
