package com.capstone.pronunciation.domain.upload.dto;

public record FastApiDispatchResponse(
		Long uploadId,
		String audioUrl,
		String fastApiUrl,
		int statusCode,
		Long resultId,
		Integer score,
		Integer voiceScore,
		Integer visionScore,
		String transcript,
		String feedbackText
) {
}
