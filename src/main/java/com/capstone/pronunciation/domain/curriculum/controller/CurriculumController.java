package com.capstone.pronunciation.domain.curriculum.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pronunciation.domain.curriculum.dto.LessonDetailResponse;
import com.capstone.pronunciation.domain.curriculum.dto.LessonSummaryResponse;
import com.capstone.pronunciation.domain.curriculum.dto.StageProgressResponse;
import com.capstone.pronunciation.domain.curriculum.service.CurriculumService;

@RequestMapping("/api/curriculum")
@RestController
public class CurriculumController {
	private final CurriculumService curriculumService;

	public CurriculumController(CurriculumService curriculumService) {
		this.curriculumService = curriculumService;
	}

	@GetMapping("/stages")
	public List<StageProgressResponse> stages(Authentication authentication) {
		return curriculumService.stages(authentication.getName());
	}

	@GetMapping("/lessons")
	public List<LessonSummaryResponse> lessonsByStage(
			Authentication authentication,
			@RequestParam String stage) {
		return curriculumService.lessonsByStageName(authentication.getName(), stage.trim().toUpperCase(Locale.ROOT));
	}

	@GetMapping("/{category}/lessons")
	public List<LessonSummaryResponse> lessonsByCategory(
			Authentication authentication,
			@PathVariable String category) {
		return curriculumService.lessonsByStageName(authentication.getName(), toStageName(category));
	}

	@GetMapping("/lessons/{lessonId}")
	public LessonDetailResponse lessonDetail(
			Authentication authentication,
			@PathVariable Long lessonId) {
		return curriculumService.questionDetail(authentication.getName(), lessonId);
	}

	@GetMapping("/{category}/lessons/{lessonId}")
	public LessonDetailResponse lessonDetailByCategory(
			Authentication authentication,
			@PathVariable String category,
			@PathVariable Long lessonId) {
		// category는 URL 가독성용. 실제 권한/해금은 lessonId로 검증.
		toStageName(category);
		return curriculumService.questionDetail(authentication.getName(), lessonId);
	}

	@PostMapping("/lessons/{lessonId}/complete")
	public void completeLesson(
			Authentication authentication,
			@PathVariable Long lessonId) {
		curriculumService.completeQuestion(authentication.getName(), lessonId, 100);
	}

	@PostMapping("/{category}/lessons/{lessonId}/complete")
	public void completeLessonByCategory(
			Authentication authentication,
			@PathVariable String category,
			@PathVariable Long lessonId) {
		toStageName(category);
		curriculumService.completeQuestion(authentication.getName(), lessonId, 100);
	}

	private static String toStageName(String category) {
		if (category == null) {
			throw new IllegalArgumentException("category는 필수입니다.");
		}

		String normalized = category.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "alphabet", "alphabets", "alpha" -> "ALPHABET";
			case "basic-pronunciation", "basic_pronunciation", "basicpronunciation", "basic" -> "BASIC_PRONUNCIATION";
			case "word", "words" -> "WORD";
			case "sentence", "sentences" -> "SENTENCE";
			default -> throw new IllegalArgumentException("지원하지 않는 category입니다: " + category);
		};
	}
}
