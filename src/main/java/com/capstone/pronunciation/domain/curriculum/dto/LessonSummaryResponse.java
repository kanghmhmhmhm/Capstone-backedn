package com.capstone.pronunciation.domain.curriculum.dto;

public record LessonSummaryResponse(
		Long id,
		Long stageId,
		String stageName,
		String sentence,
		String phoneticSymbol,
		boolean completed
) {
}
