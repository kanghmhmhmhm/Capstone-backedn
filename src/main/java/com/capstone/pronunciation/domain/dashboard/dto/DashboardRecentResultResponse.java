package com.capstone.pronunciation.domain.dashboard.dto;

import java.time.Instant;

public record DashboardRecentResultResponse(
		Long resultId,
		Long sessionId,
		Long questionId,
		String stageName,
		String sentence,
		String answer,
		String transcript,
		String selectedChoice,
		Double score,
		boolean correct,
		Instant createdAt
) {
}
