package com.capstone.pronunciation.domain.user.dto;

public record UpdateProfileRequest(
		String name,
		String currentPassword,
		String newPassword
) {
}
