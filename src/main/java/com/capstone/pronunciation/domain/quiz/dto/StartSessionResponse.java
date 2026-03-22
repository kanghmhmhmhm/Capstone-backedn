package com.capstone.pronunciation.domain.quiz.dto;

import java.time.Instant;
import java.util.List;

public record StartSessionResponse(
		Long sessionId,
		Instant startTime,
		Integer selectedLevel,
		List<QuestionDto> questions
) {
}
