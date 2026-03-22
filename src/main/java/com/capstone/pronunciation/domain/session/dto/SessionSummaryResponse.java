package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionSummaryResponse(
		Long sessionId,
		Instant startTime,
		Instant endTime,
		Integer selectedLevel,
		int totalAnswers,
		Double averageScore,
		Instant lastAnsweredAt
) {
}
