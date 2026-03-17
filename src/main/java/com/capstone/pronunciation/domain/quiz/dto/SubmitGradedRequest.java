package com.capstone.pronunciation.domain.quiz.dto;

public record SubmitGradedRequest(
		Long questionId,
		String transcript,
		Integer score,
		Integer voiceScore,
		Integer visionScore
) {
}

