package com.capstone.pronunciation.domain.dashboard.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.dashboard.dto.DashboardSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/dashboard")
@RestController
@Tag(name = "Dashboard", description = "대시보드 요약 정보 조회 API")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
	@Operation(summary = "대시보드 요약 조회", description = "현재 사용자의 학습 현황과 요약 정보를 조회합니다.")
	public DashboardSummaryResponse summary(Authentication authentication) {
		return dashboardService.summary(authentication.getName());
	}
}
