package com.capstone.pronunciation.domain.dashboard.dto;

import java.time.LocalDate;

public record DashboardHeatmapCellResponse(
		LocalDate date,
		long activityCount,
		Double averageScore
) {
}
