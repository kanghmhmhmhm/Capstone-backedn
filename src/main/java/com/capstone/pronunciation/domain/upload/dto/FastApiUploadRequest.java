package com.capstone.pronunciation.domain.upload.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiUploadRequest", description = "Spring 서버가 FastAPI 분석 서버로 전달하는 내부 요청 DTO")
public record FastApiUploadRequest(
		@Schema(description = "학습 세션 ID", example = "52")
		Long sessionId,
		@Schema(description = "문제 ID", example = "103")
		Long questionId,
		@Schema(description = "업로드 파일 ID", example = "77")
		Long uploadId,
		@Schema(description = "S3 object key", example = "audio/user@example.com/2026/04/16/uuid-sample.mp3")
		String s3Key,
		@Schema(description = "AI 서버가 내려받을 오디오 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/audio/sample.mp3")
		String audioUrl,
		@Schema(description = "문제의 정답 문장", example = "She sells seashells by the seashore.")
		String expectedText,
		@Schema(description = "사용자가 선택한 보기", example = "keep")
		String selectedChoice,
		@Schema(
				description = "MediaPipe에서 추출한 프레임별 입술/얼굴 랜드마크 데이터 배열",
				example = "[{\"frameIndex\":0,\"timestampMs\":0,\"landmarks\":[{\"x\":0.51,\"y\":0.62,\"z\":-0.01}]}]"
		)
		List<JsonNode> frames,
		@Schema(description = "분석 요청 시각", example = "2026-04-16T10:15:30Z")
		Instant submittedAt
) {
}
