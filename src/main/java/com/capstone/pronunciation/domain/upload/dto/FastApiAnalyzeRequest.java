package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiAnalyzeRequest", description = "AI 서버 /analyze OpenAPI 계약과 동일한 내부 요청 DTO")
public record FastApiAnalyzeRequest(
		@Schema(description = "정답 단어 또는 문장", example = "apple")
		String word,
		@Schema(description = "AI 서버가 직접 GET 다운로드할 presigned HTTPS URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/audio/sample.wav?...signature=...")
		String audio_url,
		@Schema(description = "MediaPipe raw frame 배열")
		List<FastApiRawFrame> frames
) {
}
