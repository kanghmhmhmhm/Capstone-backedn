package com.capstone.pronunciation.domain.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardHeatmapCellResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardRecentResultResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardReviewNoteResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardStageSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardSummaryResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardTodayQuestionResponse;
import com.capstone.pronunciation.domain.dashboard.dto.DashboardWeeklyProgressResponse;
import com.capstone.pronunciation.domain.dashboard.dto.WeakPhonemeResponse;
import com.capstone.pronunciation.domain.feedback.entity.FeedbackLog;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class DashboardService {
	private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");
	private static final int HEATMAP_DAYS = 28;

	private final UserRepository userRepository;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final CurriculumStageRepository curriculumStageRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final FeedbackLogRepository feedbackLogRepository;

	public DashboardService(
			UserRepository userRepository,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			CurriculumStageRepository curriculumStageRepository,
			QuizQuestionRepository quizQuestionRepository,
			FeedbackLogRepository feedbackLogRepository) {
		this.userRepository = userRepository;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.curriculumStageRepository = curriculumStageRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.feedbackLogRepository = feedbackLogRepository;
	}

	@Transactional(readOnly = true)
	public DashboardSummaryResponse summary(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		List<LearningSession> sessions = learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
		List<SessionResult> results = sessionResultRepository.findDetailedByUserId(user.getId());
		List<CurriculumStage> stages = curriculumStageRepository.findAllByOrderByOrderAsc();
		List<FeedbackLog> feedbackLogs = feedbackLogRepository.findByUserId(user.getId());

		long totalSessions = sessions.size();
		long completedSessions = sessions.stream()
				.filter(session -> session.getEndTime() != null)
				.count();
		Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
		long recentStudyCount = sessions.stream()
				.filter(session -> session.getStartTime() != null && !session.getStartTime().isBefore(cutoff))
				.count();

		Double averageScore = averageScore(results);
		List<WeakPhonemeResponse> weakPhonemes = buildWeakPhonemes(results);
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
				stageProgress,
				buildWeeklyProgress(results),
				results.stream().limit(5).map(this::toRecentResult).toList(),
				feedbackLogs.stream().limit(5).map(this::toReviewNote).toList(),
				buildHeatmap(results),
				todayAverageScore(results),
				recommendTodayQuestion(user)
		);
	}

	private List<WeakPhonemeResponse> buildWeakPhonemes(List<SessionResult> results) {
		return results.stream()
				.filter(result -> result.getQuestion().getPhoneticSymbol() != null && !result.getQuestion().getPhoneticSymbol().isBlank())
				.collect(Collectors.groupingBy(result -> result.getQuestion().getPhoneticSymbol()))
				.entrySet()
				.stream()
				.map(this::toWeakPhoneme)
				.sorted(Comparator.comparingDouble(WeakPhonemeResponse::averageScore)
						.thenComparing(Comparator.comparingLong(WeakPhonemeResponse::attempts).reversed()))
				.limit(3)
				.toList();
	}

	private List<DashboardWeeklyProgressResponse> buildWeeklyProgress(List<SessionResult> results) {
		LocalDate today = LocalDate.now(APP_ZONE);
		Map<LocalDate, List<SessionResult>> byDate = results.stream()
				.collect(Collectors.groupingBy(result -> LocalDate.ofInstant(result.getCreatedAt(), APP_ZONE)));

		List<DashboardWeeklyProgressResponse> progress = new ArrayList<>();
		for (int offset = 6; offset >= 0; offset--) {
			LocalDate date = today.minusDays(offset);
			List<SessionResult> dayResults = byDate.getOrDefault(date, List.of());
			progress.add(new DashboardWeeklyProgressResponse(date, dayResults.size(), averageScore(dayResults)));
		}
		return progress;
	}

	private List<DashboardHeatmapCellResponse> buildHeatmap(List<SessionResult> results) {
		LocalDate today = LocalDate.now(APP_ZONE);
		Map<LocalDate, List<SessionResult>> byDate = results.stream()
				.collect(Collectors.groupingBy(result -> LocalDate.ofInstant(result.getCreatedAt(), APP_ZONE)));

		List<DashboardHeatmapCellResponse> heatmap = new ArrayList<>();
		for (int offset = HEATMAP_DAYS - 1; offset >= 0; offset--) {
			LocalDate date = today.minusDays(offset);
			List<SessionResult> dayResults = byDate.getOrDefault(date, List.of());
			heatmap.add(new DashboardHeatmapCellResponse(date, dayResults.size(), averageScore(dayResults)));
		}
		return heatmap;
	}

	private DashboardTodayQuestionResponse recommendTodayQuestion(User user) {
		List<QuizQuestion> candidates = resolveQuestionsForLevel(user.getLevel());
		QuizQuestion recommended = candidates.stream()
				.filter(question -> !sessionResultRepository.existsByUserAndQuestion(user.getId(), question.getId()))
				.findFirst()
				.orElse(candidates.isEmpty() ? null : candidates.get(0));
		if (recommended == null) {
			return null;
		}
		return new DashboardTodayQuestionResponse(
				recommended.getId(),
				recommended.getStage().getStageName(),
				recommended.getSentence(),
				recommended.getPhoneticSymbol(),
				recommended.getDifficulty()
		);
	}

	private List<QuizQuestion> resolveQuestionsForLevel(int level) {
		if (level <= 1) {
			return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc("BASIC_PRONUNCIATION");
		}
		if (level == 2) {
			return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc("WORD");
		}
		return quizQuestionRepository.findByStage_StageNameIgnoreCaseOrderByIdAsc("Sentence Lv" + level);
	}

	private DashboardRecentResultResponse toRecentResult(SessionResult result) {
		return new DashboardRecentResultResponse(
				result.getId(),
				result.getSession().getId(),
				result.getQuestion().getId(),
				result.getQuestion().getStage().getStageName(),
				result.getQuestion().getSentence(),
				round(result.getScore()),
				result.getCreatedAt()
		);
	}

	private DashboardReviewNoteResponse toReviewNote(FeedbackLog feedbackLog) {
		return new DashboardReviewNoteResponse(
				feedbackLog.getId(),
				feedbackLog.getResult().getId(),
				feedbackLog.getResult().getQuestion().getId(),
				feedbackLog.getResult().getQuestion().getSentence(),
				feedbackLog.getFeedbackText(),
				feedbackLog.getResult().getCreatedAt()
		);
	}

	private WeakPhonemeResponse toWeakPhoneme(Map.Entry<String, List<SessionResult>> entry) {
		double avg = entry.getValue().stream()
				.mapToDouble(SessionResult::getScore)
				.average()
				.orElse(0);

		return new WeakPhonemeResponse(entry.getKey(), round(avg), entry.getValue().size());
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

	private Double todayAverageScore(List<SessionResult> results) {
		LocalDate today = LocalDate.now(APP_ZONE);
		List<SessionResult> todayResults = results.stream()
				.filter(result -> LocalDate.ofInstant(result.getCreatedAt(), APP_ZONE).equals(today))
				.toList();
		return averageScore(todayResults);
	}

	private Double averageScore(List<SessionResult> results) {
		if (results == null || results.isEmpty()) {
			return null;
		}
		return round(results.stream().mapToDouble(SessionResult::getScore).average().orElse(0));
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
