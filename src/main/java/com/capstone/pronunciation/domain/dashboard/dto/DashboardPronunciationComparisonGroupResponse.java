package com.capstone.pronunciation.domain.dashboard.dto;

import java.util.List;

public record DashboardPronunciationComparisonGroupResponse(
		String label,
		List<String> phonemes,
		long mistakeCount,
		long attempts,
		Double averageScore
) {
}
