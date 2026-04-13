package com.capstone.pronunciation.domain.dashboard.dto;

public record DashboardStageSummaryResponse(
		Long stageId,
		String stageName,
		long completedQuestions,
		long totalQuestions,
		double completionRate
) {
}
