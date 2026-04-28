package com.capstone.pronunciation.domain.curriculum.dto;

import java.util.List;

public record LessonDetailResponse(
		Long id,
		Long stageId,
		String stageName,
		int difficulty,
		String sentence,
		String phoneticSymbol,
		String answer,
		List<String> choices,
		String animationData,
		boolean completed
) {
}
