package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionEndResponse(
		Long sessionId,
		Instant startTime,
		Instant endTime,
		Integer selectedLevel
) {
}
