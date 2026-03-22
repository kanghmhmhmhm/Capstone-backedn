package com.capstone.pronunciation.domain.quiz.dto;

public record QuestionDto(
		Long questionId,
		String stageName,
		Integer difficulty,
		String sentence,
		String answer
) {
}
