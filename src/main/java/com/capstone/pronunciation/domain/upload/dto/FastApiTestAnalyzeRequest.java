package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiTestAnalyzeRequest", description = "DB 저장 없이 AI 서버 연동만 테스트할 때 사용하는 요청 DTO")
public record FastApiTestAnalyzeRequest(
		@Schema(description = "분석 대상 단어 또는 정답 문장", example = "keep")
		String word,
		@Schema(
				description = "MediaPipe에서 추출한 프레임별 입술/얼굴 랜드마크 데이터 배열",
				example = "[{\"frameIndex\":0,\"timestampMs\":0,\"landmarks\":[{\"x\":0.51,\"y\":0.62,\"z\":-0.01}]}]"
		)
		List<JsonNode> frames
) {
}
