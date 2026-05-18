package com.capstone.pronunciation.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FeedbackWavRequest", description = "AI 서버 /feedback-wav 프록시 요청")
public record FeedbackWavRequest(
		@Schema(description = "/analyze 응답의 analysisText 또는 analysis_text", example = "음성: /p/가 /b/처럼 들렸어요.\n입모양: /p/ 입모양을 정확하게 잡았어요.")
		String analysisText,
		@Schema(description = "snake_case 호환 필드. analysisText 대신 사용 가능", example = "음성: /p/가 /b/처럼 들렸어요.\n입모양: /p/ 입모양을 정확하게 잡았어요.")
		String analysis_text
) {
	public String resolvedAnalysisText() {
		if (analysisText != null && !analysisText.isBlank()) {
			return analysisText;
		}
		return analysis_text;
	}
}
