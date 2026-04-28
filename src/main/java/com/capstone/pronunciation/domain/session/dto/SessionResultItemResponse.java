package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionResultItemResponse(
		Long resultId,
		Long questionId,
		String stageName,
		String sentence,
		Double score,
		Double voiceScore,
		Double visionScore,
		String transcript,
		String selectedChoice,
		Long uploadId,
		String audioUrl,
		Instant createdAt
) {
}
