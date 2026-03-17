package com.capstone.pronunciation.domain.user.dto;

// 내 정보 응답 DTO
public record UserProfileResponse(Long userPk, String email, String name, int level) {
}
