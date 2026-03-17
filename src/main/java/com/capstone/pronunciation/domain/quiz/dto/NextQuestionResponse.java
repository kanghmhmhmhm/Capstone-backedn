package com.capstone.pronunciation.domain.quiz.dto;

public record NextQuestionResponse(
		Long questionId,
		String stageName,
		String sentence
) {
}

