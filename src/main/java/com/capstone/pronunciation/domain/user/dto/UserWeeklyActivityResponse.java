package com.capstone.pronunciation.domain.user.dto;

import java.time.LocalDate;

public record UserWeeklyActivityResponse(
		LocalDate date,
		long solvedCount,
		Double averageScore
) {
}
