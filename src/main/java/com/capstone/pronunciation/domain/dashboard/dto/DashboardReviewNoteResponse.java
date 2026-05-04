package com.capstone.pronunciation.domain.dashboard.dto;

import java.time.Instant;

public record DashboardReviewNoteResponse(
		Long feedbackId,
		Long resultId,
		Long questionId,
		String sentence,
		String feedbackText,
		Instant createdAt
) {
}
