package com.capstone.pronunciation.domain.dashboard.dto;

public record DashboardWeeklyComparisonResponse(
		Double currentWeekAverage,
		Double previousWeekAverage,
		Double scoreDelta,
		long currentWeekActivityCount,
		long previousWeekActivityCount,
		long activityDelta,
		String trend
) {
}
