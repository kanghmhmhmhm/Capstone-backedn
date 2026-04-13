package com.capstone.pronunciation.domain.dashboard.dto;

public record WeakPhonemeResponse(
		String phoneme,
		double averageScore,
		long attempts
) {
}
