package com.capstone.pronunciation.domain.curriculum.dto;

public record StageProgressResponse(
		Long stageId,
		String stageName,
		int order,
		int difficulty,
		boolean unlocked,
		boolean completed,
		long completedQuestions,
		long totalQuestions
) {
}

