package com.capstone.pronunciation.domain.dashboard.dto;

public record DashboardTodayQuestionResponse(
		Long questionId,
		String stageName,
		String sentence,
		String phoneticSymbol,
		Integer difficulty
) {
}
