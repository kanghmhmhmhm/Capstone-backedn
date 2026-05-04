package com.capstone.pronunciation.domain.dashboard.dto;

import java.time.LocalDate;

public record DashboardWeeklyProgressResponse(
		LocalDate date,
		long solvedCount,
		Double averageScore
) {
}
