package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiUploadRequest", description = "Spring 서버가 FastAPI 분석 서버로 전달하는 내부 요청 DTO")
public record FastApiUploadRequest(
		@Schema(description = "분석 대상 단어 또는 정답 문장", example = "keep")
		String word,
		@JsonProperty("audio_url")
		@Schema(description = "AI 서버가 직접 내려받을 presigned audio URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/audio/sample.mp3")
		String audioUrl,
		@Schema(
				description = "MediaPipe에서 추출한 프레임별 입술/얼굴 랜드마크 데이터 배열",
				example = "[{\"t_ms\":0,\"face_landmarks\":[{\"x\":0.51,\"y\":0.62,\"z\":-0.01}],\"face_blendshapes\":{\"mouthSmileLeft\":0.12,\"jawOpen\":0.31}}]"
		)
		List<JsonNode> frames
) {
}
