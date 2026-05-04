package com.capstone.pronunciation.domain.dashboard.dto;

import java.time.Instant;

public record DashboardRecentResultResponse(
		Long resultId,
		Long sessionId,
		Long questionId,
		String stageName,
		String sentence,
		Double score,
		Instant createdAt
) {
}
