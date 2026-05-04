package com.capstone.pronunciation.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FastApiLandmark", description = "AI 서버 OpenAPI의 Landmark 계약")
public record FastApiLandmark(
		@Schema(example = "0.59")
		double x,
		@Schema(example = "0.48")
		double y,
		@Schema(example = "-0.03")
		double z
) {
}
