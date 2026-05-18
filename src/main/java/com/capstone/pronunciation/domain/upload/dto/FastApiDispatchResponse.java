package com.capstone.pronunciation.domain.upload.dto;

import tools.jackson.databind.JsonNode;

public record FastApiDispatchResponse(
		Long uploadId,
		String audioUrl,
		String fastApiUrl,
		int statusCode,
		Long resultId,
		Long questionId,
		String animationData,
		Double score,
		Double voiceScore,
		Double visionScore,
		String transcript,
		String recognizedText,
		String selectedChoice,
		String feedbackText,
		String analysisText,
		String overallBand,
		String mildFeedback,
		String spicyFeedback,
		JsonNode phonemeFeedback,
		JsonNode mouthComparisonAssets,
		JsonNode llmFeedbackByMode,
		JsonNode llmContext,
		JsonNode feedbackPayload
) {
}
