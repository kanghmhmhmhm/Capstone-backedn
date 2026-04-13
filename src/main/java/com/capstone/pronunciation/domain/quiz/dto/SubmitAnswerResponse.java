package com.capstone.pronunciation.domain.quiz.dto;

public record SubmitAnswerResponse(
		Long resultId,
		int score,
		String expected,
		String transcript,
		Long uploadId,
		String audioUrl
) {
}
