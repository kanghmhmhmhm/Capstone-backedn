package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

@Schema(name = "FastApiTestAnalyzeRequest", description = "테스트용 업로드 오디오 AI 분석 요청 DTO")
public record FastApiTestAnalyzeRequest(
		@Schema(description = "분석 대상 단어 또는 문장", example = "apple")
		String word,
		@Schema(description = "선택 입력. 비워두면 uploadId 기준 presigned HTTPS URL을 자동 생성합니다.", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/audio/sample.wav?...signature=...")
		String audioUrl,
		@Schema(
				description = "MediaPipe raw frame 배열",
				example = "[{\"t_ms\":0,\"face_landmarks\":[{\"x\":0.59,\"y\":0.48,\"z\":-0.03}],\"face_blendshapes\":{\"jawOpen\":0.72}}]"
		)
		List<JsonNode> frames
) {
}
