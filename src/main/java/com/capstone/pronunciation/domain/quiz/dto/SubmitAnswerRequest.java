package com.capstone.pronunciation.domain.quiz.dto;

public record SubmitAnswerRequest(Long questionId, String transcript, String selectedChoice) {
}
