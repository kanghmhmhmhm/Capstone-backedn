package com.capstone.pronunciation.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.user.dto.AuthResponse;
import com.capstone.pronunciation.domain.user.dto.LoginRequest;
import com.capstone.pronunciation.domain.user.dto.SignupRequest;
import com.capstone.pronunciation.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/auth")
@RestController
@Tag(name = "Frontend - Auth", description = "프론트 실사용 API: 회원가입과 로그인을 담당합니다.")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup")
	@Operation(
			summary = "[프론트 사용] 회원가입",
			description = "이메일, 비밀번호, 이름으로 사용자 계정을 생성하고 인증 정보를 반환합니다."
	)
	public AuthResponse signup(@RequestBody SignupRequest request) {
		return userService.signup(request);
	}

	@PostMapping("/login")
	@Operation(
			summary = "[프론트 사용] 로그인",
			description = "이메일과 비밀번호를 검증한 뒤 JWT 인증 정보를 반환합니다."
	)
	public AuthResponse login(@RequestBody LoginRequest request) {
		return userService.login(request);
	}
}
