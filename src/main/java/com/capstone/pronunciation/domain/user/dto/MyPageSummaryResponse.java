package com.capstone.pronunciation.domain.user.dto;

import java.time.Instant;

public record MyPageSummaryResponse(
		Long userId,
		String email,
		String name,
		String nickname,
		int level,
		long totalSessions,
		long completedSessions,
		long totalSolvedQuestions,
		Double averageScore,
		Instant lastStudiedAt
) {
}
