package com.capstone.pronunciation.domain.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.user.dto.DeleteAccountRequest;
import com.capstone.pronunciation.domain.user.dto.MessageResponse;
import com.capstone.pronunciation.domain.user.dto.MyPageSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.UpdateProfileRequest;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/me")
@RestController
@Tag(name = "Me", description = "내 정보 조회 및 계정 관리 API")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
	public UserProfileResponse me(Authentication authentication) {
		return userService.me(authentication.getName());
	}

	@GetMapping("/summary")
	@Operation(summary = "마이페이지 요약 조회", description = "학습 진행 현황과 마이페이지 요약 정보를 조회합니다.")
	public MyPageSummaryResponse myPageSummary(Authentication authentication) {
		return userService.myPageSummary(authentication.getName());
	}

	@PatchMapping
	@Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
	public UserProfileResponse updateProfile(
			Authentication authentication,
			@RequestBody UpdateProfileRequest request) {
		return userService.updateProfile(authentication.getName(), request);
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "현재 사용자 세션을 로그아웃 처리합니다.")
	public MessageResponse logout() {
		return userService.logout();
	}

	@DeleteMapping
	@Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
	public MessageResponse deleteAccount(
			Authentication authentication,
			@RequestBody DeleteAccountRequest request) {
		return userService.deleteAccount(authentication.getName(), request);
	}
}
