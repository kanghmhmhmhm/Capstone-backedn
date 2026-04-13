package com.capstone.pronunciation.domain.session.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.session.dto.SessionDetailResponse;
import com.capstone.pronunciation.domain.session.dto.SessionEndResponse;
import com.capstone.pronunciation.domain.session.dto.SessionProgressResponse;
import com.capstone.pronunciation.domain.session.dto.SessionStartResponse;
import com.capstone.pronunciation.domain.session.dto.SessionSummaryResponse;
import com.capstone.pronunciation.domain.session.dto.SessionStartRequest;
import com.capstone.pronunciation.domain.session.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/learning/sessions")
@RestController
@Tag(name = "Learning Sessions", description = "학습 세션 생성, 조회, 진행도, 종료 API")
public class SessionController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping
	@Operation(summary = "학습 세션 시작", description = "선택한 레벨의 학습 세션을 시작하거나 진행 중인 세션을 이어서 불러옵니다. 문제 목록과 현재 진행 상태를 함께 반환합니다.")
	public SessionStartResponse startSession(
			Authentication authentication,
			@RequestBody SessionStartRequest request) {
		return sessionService.startSession(authentication.getName(), request.selectedLevel());
	}

	@PostMapping("/{sessionId}/end")
	@Operation(summary = "학습 세션 종료", description = "현재 학습 세션을 종료 처리합니다.")
	public SessionEndResponse endSession(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.endSession(authentication.getName(), sessionId);
	}

	@GetMapping
	@Operation(summary = "학습 세션 목록 조회", description = "현재 사용자의 학습 세션 목록을 최신순으로 조회합니다.")
	public List<SessionSummaryResponse> sessions(Authentication authentication) {
		return sessionService.sessions(authentication.getName());
	}

	@GetMapping("/{sessionId}")
	@Operation(summary = "학습 세션 상세 조회", description = "세션별 문제 풀이 결과와 점수 정보를 상세 조회합니다.")
	public SessionDetailResponse sessionDetail(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionDetail(authentication.getName(), sessionId);
	}

	@GetMapping("/{sessionId}/progress")
	@Operation(summary = "학습 세션 진행도 조회", description = "현재 세션의 진행도, 평균 점수, 최근 풀이 시각 등을 조회합니다.")
	public SessionProgressResponse sessionProgress(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionProgress(authentication.getName(), sessionId);
	}
}
