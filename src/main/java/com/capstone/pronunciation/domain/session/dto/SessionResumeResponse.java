package com.capstone.pronunciation.domain.session.dto;

import java.time.Instant;
import java.util.List;

import com.capstone.pronunciation.domain.quiz.dto.QuestionDto;

public record SessionResumeResponse(
		Long sessionId,
		Instant startTime,
		Instant endTime,
		Integer selectedLevel,
		Boolean inProgress,
		Long currentQuestionId,
		int answeredQuestions,
		List<QuestionDto> questions,
		List<SessionResultItemResponse> submittedResults
) {
}
