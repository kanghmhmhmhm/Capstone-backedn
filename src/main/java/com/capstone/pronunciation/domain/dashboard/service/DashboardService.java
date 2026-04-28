package com.capstone.pronunciation.domain.dashboard.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardStageSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.dto.WeakPhonemeResponse;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class DashboardService {

	private final UserRepository userRepository;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final CurriculumStageRepository curriculumStageRepository;
	private final QuizQuestionRepository quizQuestionRepository;

	public DashboardService(
			UserRepository userRepository,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			CurriculumStageRepository curriculumStageRepository,
			QuizQuestionRepository quizQuestionRepository) {
		this.userRepository = userRepository;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.curriculumStageRepository = curriculumStageRepository;
		this.quizQuestionRepository = quizQuestionRepository;
	}

	@Transactional(readOnly = true)
	public DashboardSummaryResponse summary(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		List<LearningSession> sessions = learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
		List<SessionResult> results = sessionResultRepository.findDetailedByUserId(user.getId());
		List<CurriculumStage> stages = curriculumStageRepository.findAllByOrderByOrderAsc();

		long totalSessions = sessions.size();
		long completedSessions = sessions.stream()
				.filter(session -> session.getEndTime() != null)
				.count();
		Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
		long recentStudyCount = sessions.stream()
				.filter(session -> session.getStartTime() != null && !session.getStartTime().isBefore(cutoff))
				.count();

		Double averageScore = results.isEmpty()
				? null
				: round(results.stream().mapToDouble(SessionResult::getScore).average().orElse(0));

		List<WeakPhonemeResponse> weakPhonemes = results.stream()
				.filter(result -> result.getQuestion().getPhoneticSymbol() != null && !result.getQuestion().getPhoneticSymbol().isBlank())
				.collect(Collectors.groupingBy(result -> result.getQuestion().getPhoneticSymbol()))
				.entrySet()
				.stream()
				.map(this::toWeakPhoneme)
				.sorted(Comparator.comparingDouble(WeakPhonemeResponse::averageScore)
						.thenComparing(Comparator.comparingLong(WeakPhonemeResponse::attempts).reversed()))
				.limit(3)
				.toList();

		List<DashboardStageSummaryResponse> stageProgress = stages.stream()
				.map(stage -> toStageSummary(user.getId(), stage))
				.toList();

		return new DashboardSummaryResponse(
				user.getId(),
				user.getName(),
				resolveNickname(user),
				user.getLevel(),
				totalSessions,
				completedSessions,
				recentStudyCount,
				averageScore,
				weakPhonemes,
				stageProgress
		);
	}

	private WeakPhonemeResponse toWeakPhoneme(Map.Entry<String, List<SessionResult>> entry) {
		double avg = entry.getValue().stream()
				.mapToDouble(SessionResult::getScore)
				.average()
				.orElse(0);

		return new WeakPhonemeResponse(
				entry.getKey(),
				round(avg),
				entry.getValue().size()
		);
	}

	private DashboardStageSummaryResponse toStageSummary(Long userId, CurriculumStage stage) {
		long completed = sessionResultRepository.countDistinctQuestionsByUserAndStage(userId, stage.getId());
		long total = quizQuestionRepository.countByStage_Id(stage.getId());
		return new DashboardStageSummaryResponse(
				stage.getId(),
				stage.getStageName(),
				completed,
				total,
				total == 0 ? 0 : round((double) completed * 100.0 / total)
		);
	}

	private static double round(double value) {
		return Math.round(value * 10.0) / 10.0;
	}

	private static String resolveNickname(User user) {
		if (user.getNickname() != null && !user.getNickname().isBlank()) {
			return user.getNickname();
		}
		return user.getName();
	}
}
