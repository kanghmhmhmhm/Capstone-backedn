package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiRawFrame", description = "AI 서버 OpenAPI의 RawFrame 계약")
public record FastApiRawFrame(
		@Schema(description = "WAV 녹음 시작 기준 경과 시간(ms)", example = "33")
		double t_ms,
		@Schema(description = "MediaPipe face mesh 478개 랜드마크")
		List<FastApiLandmark> face_landmarks,
		@Schema(description = "blendshape score 맵", example = "{\"jawOpen\":0.72,\"mouthFunnel\":0.15}")
		Map<String, Double> face_blendshapes
) {
}
