package com.capstone.pronunciation.domain.quiz.dto;

public record SubmitGradedRequest(
		Long questionId,
		String transcript,
		String selectedChoice,
		Double score,
		Double voiceScore,
		Double visionScore
) {
}
