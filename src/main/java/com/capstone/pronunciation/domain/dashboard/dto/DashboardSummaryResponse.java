package com.capstone.pronunciation.domain.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
		Long userId,
		String name,
		String nickname,
		int level,
		long totalSessions,
		long completedSessions,
		long recentStudyCount,
		Double averageScore,
		List<WeakPhonemeResponse> weakPhonemes,
		List<DashboardStageSummaryResponse> stageProgress,
		List<DashboardWeeklyProgressResponse> weeklyProgress,
		List<DashboardRecentResultResponse> recentResults,
		List<DashboardReviewNoteResponse> reviewNotes,
		List<DashboardHeatmapCellResponse> calendarHeatmap,
		Double dailyScore,
		DashboardTodayQuestionResponse todayQuestion
) {
}
