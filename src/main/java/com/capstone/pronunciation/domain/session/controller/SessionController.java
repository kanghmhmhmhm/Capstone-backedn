package com.capstone.pronunciation.domain.session.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.session.dto.SessionEndResponse;
import com.capstone.pronunciation.domain.session.dto.SessionProgressResponse;
import com.capstone.pronunciation.domain.session.dto.SessionResumeResponse;
import com.capstone.pronunciation.domain.session.dto.SessionStartRequest;
import com.capstone.pronunciation.domain.session.dto.SessionStartResponse;
import com.capstone.pronunciation.domain.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/learning/sessions")
@RestController
@Tag(name = "Frontend - Learning Sessions", description = "프론트 실사용 API: 학습 세션 시작, 진행도 확인, 종료 흐름을 담당합니다.")
public class SessionController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping
	@Operation(
			summary = "[프론트 사용] 학습 세션 시작",
			description = "선택한 레벨의 학습 세션을 시작하거나 진행 중인 세션을 이어서 불러옵니다. 문제 목록과 현재 진행 상태를 함께 반환합니다."
	)
	public SessionStartResponse startSession(
			Authentication authentication,
			@RequestBody SessionStartRequest request) {
		return sessionService.startSession(authentication.getName(), request.selectedLevel());
	}

	@PostMapping("/{sessionId}/end")
	@Operation(
			summary = "[프론트 사용] 학습 세션 종료",
			description = "현재 학습 세션을 종료 처리합니다. 결과 상세 조회는 /api/results/sessions/{sessionId} 를 사용합니다."
	)
	public SessionEndResponse endSession(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.endSession(authentication.getName(), sessionId);
	}

	@GetMapping("/{sessionId}")
	@Operation(
			summary = "[프론트 사용] 학습 세션 상세 재진입",
			description = "새로고침 또는 학습 화면 재진입 시 세션의 문제 목록, 현재 문제, 제출된 결과를 한 번에 복원합니다."
	)
	public SessionResumeResponse sessionResume(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionResume(authentication.getName(), sessionId);
	}

	@GetMapping("/{sessionId}/progress")
	@Operation(
			summary = "[프론트 사용] 학습 세션 진행도 조회",
			description = "현재 세션의 진행도, 평균 점수, 최근 풀이 시각 등을 조회합니다. 활성 학습 화면에서 사용합니다."
	)
	public SessionProgressResponse sessionProgress(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionProgress(authentication.getName(), sessionId);
	}
}
