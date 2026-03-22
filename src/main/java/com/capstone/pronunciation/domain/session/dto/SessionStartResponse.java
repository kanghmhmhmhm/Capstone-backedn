package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;

public record SessionStartResponse(
		Long sessionId,
		Instant startTime,
		Integer selectedLevel
) {
}
