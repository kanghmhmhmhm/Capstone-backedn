package com.capstone.pronunciation.domain.user.dto;

public record UpdateProfileRequest(
		String name,
		String nickname,
		String currentPassword,
		String newPassword
) {
}
