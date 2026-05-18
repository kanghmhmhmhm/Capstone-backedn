package com.capstone.pronunciation.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FeedbackWavResponse", description = "AI 서버 /feedback-wav 응답")
public record FeedbackWavResponse(
		@Schema(description = "다정한 햄스터 wav base64")
		String mild_wav_base64,
		@Schema(description = "빈정대는 햄스터 wav base64")
		String spicy_wav_base64,
		@Schema(description = "오디오 MIME 타입", example = "audio/wav")
		String audio_format
) {
}
