package com.capstone.pronunciation.domain.user.dto;

public record UserBadgeResponse(
		String code,
		String label,
		String description,
		boolean earned
) {
}
