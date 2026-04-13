package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;
import java.util.List;

import com.capstone.pronunciation.domain.quiz.dto.QuestionDto;

public record SessionStartResponse(
		Long sessionId,
		Instant startTime,
		Integer selectedLevel,
		Boolean inProgress,
		Long currentQuestionId,
		List<QuestionDto> questions
) {
}
