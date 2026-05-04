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
import com.capstone.pronunciation.domain.user.dto.MyPageQuestionSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.MyPageSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.UpdateNicknameRequest;
import com.capstone.pronunciation.domain.user.dto.UpdateProfileRequest;
import com.capstone.pronunciation.domain.user.dto.UserBadgeResponse;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.user.dto.UserSettingsRequest;
import com.capstone.pronunciation.domain.user.dto.UserSettingsResponse;
import com.capstone.pronunciation.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/me")
@RestController
@Tag(name = "Frontend - Me", description = "프론트 실사용 API: 내 정보 조회, 마이페이지 요약, 수정, 로그아웃, 탈퇴를 담당합니다.")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@Operation(
			summary = "[프론트 사용] 내 정보 조회",
			description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
	)
	public UserProfileResponse me(Authentication authentication) {
		return userService.me(authentication.getName());
	}

	@GetMapping("/summary")
	@Operation(
			summary = "[프론트 사용] 마이페이지 요약 조회",
			description = "학습 진행 현황과 마이페이지 요약 정보를 조회합니다."
	)
	public MyPageSummaryResponse myPageSummary(Authentication authentication) {
		return userService.myPageSummary(authentication.getName());
	}

	@GetMapping("/badges")
	@Operation(
			summary = "[프론트 사용] 내 배지 조회",
			description = "마이페이지 배지 섹션에 표시할 획득 배지 목록을 조회합니다."
	)
	public java.util.List<UserBadgeResponse> myBadges(Authentication authentication) {
		return userService.myBadges(authentication.getName());
	}

	@GetMapping("/questions")
	@Operation(
			summary = "[프론트 사용] 최근 학습 문제 조회",
			description = "마이페이지의 Asked Questions/최근 시도 문제 섹션에 표시할 데이터를 조회합니다."
	)
	public java.util.List<MyPageQuestionSummaryResponse> myQuestions(
			Authentication authentication,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
		return userService.myQuestions(authentication.getName(), limit);
	}

	@GetMapping("/settings")
	@Operation(
			summary = "[프론트 사용] 내 설정 조회",
			description = "설정 화면의 현재 학습/사운드/코치 톤 설정을 조회합니다."
	)
	public UserSettingsResponse mySettings(Authentication authentication) {
		return userService.mySettings(authentication.getName());
	}

	@PatchMapping("/settings")
	@Operation(
			summary = "[프론트 사용] 내 설정 수정",
			description = "설정 화면의 알림, 사운드, 입모양 가이드, 코치 톤 옵션을 수정합니다."
	)
	public UserSettingsResponse updateSettings(
			Authentication authentication,
			@RequestBody UserSettingsRequest request) {
		return userService.updateSettings(authentication.getName(), request);
	}

	@PatchMapping
	@Operation(
			summary = "[프론트 사용] 내 정보 수정",
			description = "현재 로그인한 사용자의 프로필 정보를 수정합니다."
	)
	public UserProfileResponse updateProfile(
			Authentication authentication,
			@RequestBody UpdateProfileRequest request) {
		return userService.updateProfile(authentication.getName(), request);
	}

	@PatchMapping("/nickname")
	@Operation(
			summary = "[프론트 사용] 닉네임 변경",
			description = "현재 로그인한 사용자의 닉네임만 별도로 수정합니다."
	)
	public UserProfileResponse updateNickname(
			Authentication authentication,
			@RequestBody UpdateNicknameRequest request) {
		return userService.updateNickname(authentication.getName(), request);
	}

	@PostMapping("/logout")
	@Operation(
			summary = "[프론트 사용] 로그아웃",
			description = "현재 사용자 세션을 로그아웃 처리합니다."
	)
	public MessageResponse logout() {
		return userService.logout();
	}

	@DeleteMapping
	@Operation(
			summary = "[프론트 사용] 회원 탈퇴",
			description = "현재 로그인한 사용자의 계정을 삭제합니다."
	)
	public MessageResponse deleteAccount(
			Authentication authentication,
			@RequestBody DeleteAccountRequest request) {
		return userService.deleteAccount(authentication.getName(), request);
	}
}
