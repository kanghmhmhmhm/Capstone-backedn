package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionResultItemResponse(
		Long resultId,
		Long questionId,
		String stageName,
		String sentence,
		Integer score,
		Integer voiceScore,
		Integer visionScore,
		String transcript,
		Instant createdAt
) {
}
