package com.capstone.pronunciation.domain.ranking.dto;

public record RankingEntryResponse(
		int rank,
		Long userId,
		String nickname,
		int level,
		Double score,
		Double averageScore,
		long totalSolvedQuestions,
		String badge,
		String avatarSeed,
		boolean me
) {
}
