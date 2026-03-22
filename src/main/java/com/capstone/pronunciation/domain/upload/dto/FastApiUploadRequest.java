package com.capstone.pronunciation.domain.upload.dto;

import java.time.Instant;

public record FastApiUploadRequest(
		Long sessionId,
		Long questionId,
		Long uploadId,
		String s3Key,
		String audioUrl,
		String expectedText,
		Instant submittedAt
) {
}
