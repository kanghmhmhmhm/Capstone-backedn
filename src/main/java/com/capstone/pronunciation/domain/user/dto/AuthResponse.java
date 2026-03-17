package com.capstone.pronunciation.domain.user.dto;

// 로그인/회원가입 성공 응답 DTO
public record AuthResponse(String accessToken, UserProfileResponse user) {
}
