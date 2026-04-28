package com.capstone.pronunciation.domain.user.dto;

// 회원가입 요청 DTO
public record SignupRequest(String email, String password, String name, String nickname) {
}
