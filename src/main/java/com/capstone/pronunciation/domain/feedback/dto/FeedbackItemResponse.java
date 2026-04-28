package com.capstone.pronunciation.domain.feedback.dto;

public record FeedbackItemResponse(
		Long feedbackId,
		Long sessionId,
		Long resultId,
		Long questionId,
		String sentence,
		Double score,
		String mode,
		String feedbackText
) {
}
