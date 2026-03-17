package com.capstone.pronunciation.domain.curriculum.dto;

public record LessonDetailResponse(
		Long id,
		Long stageId,
		String stageName,
		String sentence,
		String phoneticSymbol,
		String answer,
		boolean completed
) {
}
