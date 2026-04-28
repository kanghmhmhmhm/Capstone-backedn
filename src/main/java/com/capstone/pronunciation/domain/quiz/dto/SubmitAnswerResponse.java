package com.capstone.pronunciation.domain.quiz.dto;

public record SubmitAnswerResponse(
		Long resultId,
		Double score,
		String expected,
		String transcript,
		String selectedChoice,
		Long uploadId,
		String audioUrl
) {
}
