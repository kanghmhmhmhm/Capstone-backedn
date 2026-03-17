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
		Integer score,
		Integer voiceScore,
		Integer visionScore,
		String transcript,
		Instant createdAt
) {
}

