package com.capstone.pronunciation.domain.quiz.dto;

import java.time.Instant;

public record StartSessionResponse(Long sessionId, Instant startTime) {
}

