package com.capstone.pronunciation.domain.user.dto;

import java.time.Instant;

public record MyPageQuestionSummaryResponse(
		Long questionId,
		String stageName,
		String sentence,
		Double score,
		Instant attemptedAt
) {
}
