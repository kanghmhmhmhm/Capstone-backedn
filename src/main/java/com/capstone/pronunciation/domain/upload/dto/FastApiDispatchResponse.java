package com.capstone.pronunciation.domain.upload.dto;

import tools.jackson.databind.JsonNode;

public record FastApiDispatchResponse(
		Long uploadId,
		String audioUrl,
		String fastApiUrl,
		int statusCode,
		Long resultId,
		Double score,
		Double voiceScore,
		Double visionScore,
		String transcript,
		String recognizedText,
		String selectedChoice,
		String feedbackText,
		String overallBand,
		JsonNode phonemeFeedback,
		JsonNode mouthComparisonAssets,
		JsonNode llmFeedbackByMode,
		JsonNode feedbackPayload
) {
}
