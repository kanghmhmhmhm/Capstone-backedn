package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SendToFastApiRequest", description = "업로드된 오디오를 AI 서버로 분석 요청할 때 사용하는 요청 DTO")
public record SendToFastApiRequest(
		@Schema(description = "학습 세션 ID", example = "52")
		Long sessionId,
		@Schema(description = "분석 대상 문제 ID", example = "103")
		Long questionId,
		@Schema(description = "문제의 정답 문장", example = "She sells seashells by the seashore.")
		String expectedText,
		@Schema(description = "사용자가 선택한 보기", example = "keep")
		String selectedChoice,
		@Schema(
				description = "MediaPipe에서 추출한 프레임별 입술/얼굴 랜드마크 데이터 배열",
				example = "[{\"frameIndex\":0,\"timestampMs\":0,\"landmarks\":[{\"x\":0.51,\"y\":0.62,\"z\":-0.01},{\"x\":0.54,\"y\":0.64,\"z\":-0.02}]},{\"frameIndex\":1,\"timestampMs\":33,\"landmarks\":[{\"x\":0.50,\"y\":0.61,\"z\":-0.01},{\"x\":0.55,\"y\":0.65,\"z\":-0.02}]}]"
		)
		List<JsonNode> frames
) {
}
