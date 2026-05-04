package com.capstone.pronunciation.domain.ranking.dto;

import java.time.Instant;
import java.util.List;

public record RankingResponse(
		Instant generatedAt,
		RankingEntryResponse myRank,
		List<RankingEntryResponse> rankings
) {
}
