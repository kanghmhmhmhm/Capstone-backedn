package com.capstone.pronunciation.domain.history.dto;

import java.time.Instant;

public record StudyHistoryItemResponse(
		Long resultId,
		Long sessionId,
		Instant sessionStartTime,
		Instant sessionEndTime,
		String stageName,
		Long questionId,
		String sentence,
		Double score,
		Double voiceScore,
		Double visionScore,
		String transcript,
		String selectedChoice,
		Instant createdAt
) {
}
