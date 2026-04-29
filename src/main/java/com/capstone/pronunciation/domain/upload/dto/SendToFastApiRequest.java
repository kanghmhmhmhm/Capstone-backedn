package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import tools.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SendToFastApiRequest", description = "업로드된 오디오를 AI 서버로 분석 요청할 때 사용하는 요청 DTO")
public record SendToFastApiRequest(
		@Schema(description = "학습 세션 ID", example = "52")
		Long sessionId,
		@Schema(description = "분석 대상 문제 ID", example = "103")
		Long questionId,
		@Schema(description = "AI 서버로 전달할 분석 대상 단어 또는 정답 문장", example = "keep")
		String word,
		@Schema(description = "사용자가 선택한 보기", example = "keep")
		String selectedChoice,
		@Schema(
				description = "MediaPipe에서 추출한 프레임별 입술/얼굴 랜드마크 데이터 배열",
				example = "[{\"t_ms\":0,\"face_landmarks\":[{\"x\":0.51,\"y\":0.62,\"z\":-0.01},{\"x\":0.54,\"y\":0.64,\"z\":-0.02}],\"face_blendshapes\":{\"mouthSmileLeft\":0.12,\"mouthSmileRight\":0.08,\"jawOpen\":0.31}}]"
		)
		List<JsonNode> frames
) {
}
