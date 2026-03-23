package com.capstone.pronunciation.domain.upload.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public record FastApiUploadRequest(
		Long sessionId,
		Long questionId,
		Long uploadId,
		String s3Key,
		String audioUrl,
		String expectedText,
		List<JsonNode> frames,
		Instant submittedAt
) {
}
