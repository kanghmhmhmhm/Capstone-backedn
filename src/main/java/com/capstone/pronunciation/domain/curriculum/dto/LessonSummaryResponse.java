package com.capstone.pronunciation.domain.curriculum.dto;

public record LessonSummaryResponse(
		Long id,
		Long stageId,
		String stageName,
		int difficulty,
		String sentence,
		String phoneticSymbol,
		String animationData,
		String wordAudioUrl,
		boolean completed
) {
}
