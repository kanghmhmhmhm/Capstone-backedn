package com.capstone.pronunciation.domain.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
		Long userId,
		String name,
		int level,
		long totalSessions,
		long completedSessions,
		long recentStudyCount,
		Double averageScore,
		List<WeakPhonemeResponse> weakPhonemes,
		List<DashboardStageSummaryResponse> stageProgress
) {
}
