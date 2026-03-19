package com.capstone.pronunciation.domain.curriculum.dto;

public record LessonDetailResponse(
		Long id,
		Long stageId,
		String stageName,
		int difficulty,
		String sentence,
		String phoneticSymbol,
		String answer,
		boolean completed
) {
}
