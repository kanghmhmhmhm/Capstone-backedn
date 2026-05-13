package com.capstone.pronunciation.domain.dashboard.dto;

public record DashboardWrongWordResponse(
		String word,
		long mistakeCount,
		long attempts,
		Double averageScore
) {
}
