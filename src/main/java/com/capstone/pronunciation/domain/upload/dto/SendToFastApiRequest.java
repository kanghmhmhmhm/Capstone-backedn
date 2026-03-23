package com.capstone.pronunciation.domain.upload.dto;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public record SendToFastApiRequest(
		Long sessionId,
		Long questionId,
		String expectedText,
		List<JsonNode> frames
) {
}
