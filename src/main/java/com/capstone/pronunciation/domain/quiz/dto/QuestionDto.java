package com.capstone.pronunciation.domain.quiz.dto;

import java.util.List;

public record QuestionDto(
		Long questionId,
		String stageName,
		Integer difficulty,
		String sentence,
		String answer,
		List<String> choices,
		String animationData,
		Boolean solved
) {
}
