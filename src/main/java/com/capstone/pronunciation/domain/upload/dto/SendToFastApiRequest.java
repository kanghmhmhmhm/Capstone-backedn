package com.capstone.pronunciation.domain.upload.dto;

public record SendToFastApiRequest(
		Long sessionId,
		Long questionId,
		String expectedText
) {
}
