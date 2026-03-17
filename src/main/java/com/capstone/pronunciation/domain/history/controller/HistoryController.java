package com.capstone.pronunciation.domain.history.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.history.dto.StudyHistoryItemResponse;
import com.capstone.pronunciation.domain.history.service.HistoryService;

@RequestMapping("/api/history")
@RestController
public class HistoryController {
	private final HistoryService historyService;

	public HistoryController(HistoryService historyService) {
		this.historyService = historyService;
	}

	@GetMapping("/results")
	public List<StudyHistoryItemResponse> recentResults(
			Authentication authentication,
			@RequestParam(defaultValue = "50") int limit) {
		return historyService.recentResults(authentication.getName(), limit);
	}
}

