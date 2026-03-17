package com.capstone.pronunciation.domain.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.user.dto.AuthResponse;
import com.capstone.pronunciation.domain.user.dto.LoginRequest;
import com.capstone.pronunciation.domain.user.dto.SignupRequest;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.user.service.UserService;

@RequestMapping("/api/users")
@RestController
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// 회원가입
	@PostMapping("/signup")
	public AuthResponse signup(@RequestBody SignupRequest request) {
		return userService.signup(request);
	}

	// 로그인
	@PostMapping("/login")
	public AuthResponse login(@RequestBody LoginRequest request) {
		return userService.login(request);
	}

	// 내 정보(Authorization: Bearer 토큰 필요)
	@GetMapping("/me")
	public UserProfileResponse me(Authentication authentication) {
		return userService.me(authentication.getName());
	}
}
