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

@RequestMapping("/api/sessions")
@RestController
public class SessionController {

	private final SessionService sessionService;

	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping
	public SessionStartResponse startSession(
			Authentication authentication,
			@RequestBody SessionStartRequest request) {
		return sessionService.startSession(authentication.getName(), request.selectedLevel());
	}

	@PostMapping("/{sessionId}/end")
	public SessionEndResponse endSession(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.endSession(authentication.getName(), sessionId);
	}

	@GetMapping
	public List<SessionSummaryResponse> sessions(Authentication authentication) {
		return sessionService.sessions(authentication.getName());
	}

	@GetMapping("/{sessionId}")
	public SessionDetailResponse sessionDetail(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionDetail(authentication.getName(), sessionId);
	}

	@GetMapping("/{sessionId}/progress")
	public SessionProgressResponse sessionProgress(Authentication authentication, @PathVariable Long sessionId) {
		return sessionService.sessionProgress(authentication.getName(), sessionId);
	}
}
