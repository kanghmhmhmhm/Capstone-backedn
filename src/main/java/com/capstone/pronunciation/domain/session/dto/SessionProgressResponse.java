package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionProgressResponse(
		Long sessionId,
		Integer selectedLevel,
		int answeredQuestions,
		Double averageScore,
		Instant startedAt,
		Instant endedAt,
		Instant lastAnsweredAt,
		boolean finished
) {
}
