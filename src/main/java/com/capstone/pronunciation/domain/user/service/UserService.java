package com.capstone.pronunciation.domain.user.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.repository.UserProgressRepository;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.AnswerSubmissionRepository;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.PronunciationScoreRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.user.dto.AuthResponse;
import com.capstone.pronunciation.domain.user.dto.DeleteAccountRequest;
import com.capstone.pronunciation.domain.user.dto.LoginRequest;
import com.capstone.pronunciation.domain.user.dto.MessageResponse;
import com.capstone.pronunciation.domain.user.dto.MyPageQuestionSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.MyPageSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.SignupRequest;
import com.capstone.pronunciation.domain.user.dto.UpdateNicknameRequest;
import com.capstone.pronunciation.domain.user.dto.UpdateProfileRequest;
import com.capstone.pronunciation.domain.user.dto.UserBadgeResponse;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.user.dto.UserSettingsRequest;
import com.capstone.pronunciation.domain.user.dto.UserSettingsResponse;
import com.capstone.pronunciation.domain.user.dto.UserWeeklyActivityResponse;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.entity.UserSettings;
import com.capstone.pronunciation.domain.user.repository.UserRepository;
import com.capstone.pronunciation.domain.user.repository.UserSettingsRepository;
import com.capstone.pronunciation.global.config.JwtUtil;
import com.capstone.pronunciation.global.exception.ConflictException;
import com.capstone.pronunciation.global.exception.UnauthorizedException;

@Service
public class UserService {
	private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");
	private static final int WEEKLY_ACTIVITY_DAYS = 7;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final FeedbackLogRepository feedbackLogRepository;
	private final PronunciationScoreRepository pronunciationScoreRepository;
	private final AnswerSubmissionRepository answerSubmissionRepository;
	private final UserProgressRepository userProgressRepository;
	private final UploadFileRepository uploadFileRepository;
	private final UserSettingsRepository userSettingsRepository;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			FeedbackLogRepository feedbackLogRepository,
			PronunciationScoreRepository pronunciationScoreRepository,
			AnswerSubmissionRepository answerSubmissionRepository,
			UserProgressRepository userProgressRepository,
			UploadFileRepository uploadFileRepository,
			UserSettingsRepository userSettingsRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.feedbackLogRepository = feedbackLogRepository;
		this.pronunciationScoreRepository = pronunciationScoreRepository;
		this.answerSubmissionRepository = answerSubmissionRepository;
		this.userProgressRepository = userProgressRepository;
		this.uploadFileRepository = uploadFileRepository;
		this.userSettingsRepository = userSettingsRepository;
	}

	@Transactional
	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("이미 사용 중인 이메일입니다.");
		}

		String password = passwordEncoder.encode(requireText(request.password(), "비밀번호"));
		String name = requireText(request.name(), "이름");
		String nickname = requireText(request.nickname(), "닉네임");

		User user = new User(email, password, name, nickname, 1);
		userRepository.save(user);
		userSettingsRepository.save(new UserSettings(user));

		String accessToken = JwtUtil.issueAccessToken(user.getEmail());
		return new AuthResponse(accessToken, toUserProfileResponse(user));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

		if (!passwordEncoder.matches(requireText(request.password(), "비밀번호"), user.getPassword())) {
			throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		String accessToken = JwtUtil.issueAccessToken(user.getEmail());
		return new AuthResponse(accessToken, toUserProfileResponse(user));
	}

	@Transactional(readOnly = true)
	public UserProfileResponse me(String email) {
		return toUserProfileResponse(getUser(email));
	}

	@Transactional(readOnly = true)
	public MyPageSummaryResponse myPageSummary(String email) {
		User user = getUser(email);
		List<LearningSession> sessions = learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
		List<SessionResult> results = sessionResultRepository.findDetailedByUserId(user.getId());

		long totalSessions = sessions.size();
		long completedSessions = sessions.stream()
				.filter(session -> session.getEndTime() != null)
				.count();
		long totalSolvedQuestions = results.size();
		Double averageScore = averageScore(results);
		Instant lastStudiedAt = results.stream()
				.map(SessionResult::getCreatedAt)
				.max(Instant::compareTo)
				.orElse(null);

		return new MyPageSummaryResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				resolveNickname(user),
				user.getLevel(),
				totalSessions,
				completedSessions,
				totalSolvedQuestions,
				averageScore,
				lastStudiedAt,
				calculateLearningStreak(sessions),
				buildBadges(sessions, results),
				toQuestionSummaries(results, 5),
				buildWeeklyActivity(results),
				findBestWeakPhoneme(results),
				calculateImprovementRate(results)
		);
	}

	@Transactional(readOnly = true)
	public List<UserBadgeResponse> myBadges(String email) {
		User user = getUser(email);
		List<LearningSession> sessions = learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
		List<SessionResult> results = sessionResultRepository.findDetailedByUserId(user.getId());
		return buildBadges(sessions, results);
	}

	@Transactional(readOnly = true)
	public List<MyPageQuestionSummaryResponse> myQuestions(String email, int limit) {
		if (limit <= 0 || limit > 100) {
			throw new IllegalArgumentException("limit은 1~100 사이여야 합니다.");
		}
		User user = getUser(email);
		List<SessionResult> results = sessionResultRepository.findRecentByUserId(user.getId(), limit);
		return toQuestionSummaries(results, limit);
	}

	@Transactional(readOnly = true)
	public UserSettingsResponse mySettings(String email) {
		User user = getUser(email);
		return toSettingsResponse(resolveSettings(user));
	}

	@Transactional
	public UserSettingsResponse updateSettings(String email, UserSettingsRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
		}

		UserSettings settings = resolveSettings(getUser(email));
		if (request.dailyReminderEnabled() != null) {
			settings.setDailyReminderEnabled(request.dailyReminderEnabled());
		}
		if (request.soundEffectsEnabled() != null) {
			settings.setSoundEffectsEnabled(request.soundEffectsEnabled());
		}
		if (request.mouthGuideOverlayEnabled() != null) {
			settings.setMouthGuideOverlayEnabled(request.mouthGuideOverlayEnabled());
		}
		if (request.autoPlayPronunciationEnabled() != null) {
			settings.setAutoPlayPronunciationEnabled(request.autoPlayPronunciationEnabled());
		}
		if (request.preferredCoachTone() != null && !request.preferredCoachTone().isBlank()) {
			settings.setPreferredCoachTone(normalizeCoachTone(request.preferredCoachTone()));
		}

		return toSettingsResponse(userSettingsRepository.save(settings));
	}

	@Transactional
	public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
		User user = getUser(email);
		if (request == null) {
			throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
		}

		if (request.name() != null && !request.name().isBlank()) {
			user.setName(requireText(request.name(), "이름"));
		}
		if (request.nickname() != null && !request.nickname().isBlank()) {
			user.setNickname(requireText(request.nickname(), "닉네임"));
		}

		boolean wantsPasswordChange = request.newPassword() != null && !request.newPassword().isBlank();
		if (wantsPasswordChange) {
			String currentPassword = requireText(request.currentPassword(), "현재 비밀번호");
			if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
				throw new UnauthorizedException("현재 비밀번호가 올바르지 않습니다.");
			}
			user.setPassword(passwordEncoder.encode(requireText(request.newPassword(), "새 비밀번호")));
		}

		return toUserProfileResponse(user);
	}

	@Transactional
	public UserProfileResponse updateNickname(String email, UpdateNicknameRequest request) {
		User user = getUser(email);
		if (request == null) {
			throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
		}

		user.setNickname(requireText(request.nickname(), "닉네임"));
		return toUserProfileResponse(user);
	}

	@Transactional(readOnly = true)
	public MessageResponse logout() {
		return new MessageResponse("로그아웃되었습니다. 클라이언트에서 토큰을 삭제해주세요.");
	}

	@Transactional
	public MessageResponse deleteAccount(String email, DeleteAccountRequest request) {
		User user = getUser(email);
		if (request == null) {
			throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
		}

		String password = requireText(request.password(), "비밀번호");
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
		}

		userSettingsRepository.findByUser_Id(user.getId()).ifPresent(userSettingsRepository::delete);
		feedbackLogRepository.deleteByUserId(user.getId());
		pronunciationScoreRepository.deleteByUserId(user.getId());
		answerSubmissionRepository.deleteByUserId(user.getId());
		sessionResultRepository.deleteByUserId(user.getId());
		learningSessionRepository.deleteByUserId(user.getId());
		userProgressRepository.deleteByUser_Id(user.getId());
		uploadFileRepository.deleteByUser_Id(user.getId());
		userRepository.delete(user);

		return new MessageResponse("회원 탈퇴가 완료되었습니다.");
	}

	private User getUser(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("인증 정보가 올바르지 않습니다."));
	}

	private UserSettings resolveSettings(User user) {
		return userSettingsRepository.findByUser_Id(user.getId())
				.orElseGet(() -> userSettingsRepository.save(new UserSettings(user)));
	}

	private List<UserWeeklyActivityResponse> buildWeeklyActivity(List<SessionResult> results) {
		LocalDate today = LocalDate.now(APP_ZONE);
		Map<LocalDate, List<SessionResult>> byDate = results.stream()
				.collect(Collectors.groupingBy(result -> LocalDate.ofInstant(result.getCreatedAt(), APP_ZONE)));

		List<UserWeeklyActivityResponse> activity = new ArrayList<>();
		for (int offset = WEEKLY_ACTIVITY_DAYS - 1; offset >= 0; offset--) {
			LocalDate date = today.minusDays(offset);
			List<SessionResult> dayResults = byDate.getOrDefault(date, List.of());
			activity.add(new UserWeeklyActivityResponse(
					date,
					dayResults.size(),
					averageScore(dayResults)
			));
		}
		return activity;
	}

	private List<MyPageQuestionSummaryResponse> toQuestionSummaries(List<SessionResult> results, int limit) {
		return results.stream()
				.limit(limit)
				.map(result -> new MyPageQuestionSummaryResponse(
						result.getQuestion().getId(),
						result.getQuestion().getStage().getStageName(),
						result.getQuestion().getSentence(),
						round(result.getScore()),
						result.getCreatedAt()
				))
				.toList();
	}

	private List<UserBadgeResponse> buildBadges(List<LearningSession> sessions, List<SessionResult> results) {
		long streak = calculateLearningStreak(sessions);
		long totalSolvedQuestions = results.size();
		Double averageScore = averageScore(results);
		Set<String> phonemes = results.stream()
				.map(result -> result.getQuestion().getPhoneticSymbol())
				.filter(symbol -> symbol != null && !symbol.isBlank())
				.collect(Collectors.toCollection(HashSet::new));

		return List.of(
				new UserBadgeResponse(
						"BEGINNER_LEARNER",
						"Beginner Learner",
						"첫 문제를 해결하면 획득합니다.",
						totalSolvedQuestions >= 1
				),
				new UserBadgeResponse(
						"CONSISTENT_LEARNER",
						"Consistent Learner",
						"3일 연속 학습하면 획득합니다.",
						streak >= 3
				),
				new UserBadgeResponse(
						"DAILY_MASTER",
						"Daily Master",
						"평균 90점 이상으로 5문제 이상 풀면 획득합니다.",
						averageScore != null && averageScore >= 90.0 && totalSolvedQuestions >= 5
				),
				new UserBadgeResponse(
						"PHONEME_HUNTER",
						"Phoneme Hunter",
						"서로 다른 음소 5개 이상을 연습하면 획득합니다.",
						phonemes.size() >= 5
				),
				new UserBadgeResponse(
						"VOCAB_EXPLORER",
						"Vocab Explorer",
						"20문제 이상 해결하면 획득합니다.",
						totalSolvedQuestions >= 20
				)
		);
	}

	private long calculateLearningStreak(List<LearningSession> sessions) {
		Set<LocalDate> studiedDates = sessions.stream()
				.map(LearningSession::getStartTime)
				.filter(instant -> instant != null)
				.map(instant -> LocalDate.ofInstant(instant, APP_ZONE))
				.collect(Collectors.toSet());

		long streak = 0;
		LocalDate cursor = LocalDate.now(APP_ZONE);
		while (studiedDates.contains(cursor)) {
			streak++;
			cursor = cursor.minusDays(1);
		}
		return streak;
	}

	private String findBestWeakPhoneme(List<SessionResult> results) {
		return results.stream()
				.filter(result -> result.getQuestion().getPhoneticSymbol() != null && !result.getQuestion().getPhoneticSymbol().isBlank())
				.collect(Collectors.groupingBy(result -> result.getQuestion().getPhoneticSymbol()))
				.entrySet()
				.stream()
				.min(Comparator.comparingDouble(entry -> entry.getValue().stream()
						.mapToDouble(SessionResult::getScore)
						.average()
						.orElse(100.0)))
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	private Double calculateImprovementRate(List<SessionResult> results) {
		if (results.size() < 2) {
			return null;
		}

		List<SessionResult> ascending = results.stream()
				.sorted(Comparator.comparing(SessionResult::getCreatedAt))
				.toList();
		int pivot = Math.max(1, ascending.size() / 2);
		List<SessionResult> earlier = ascending.subList(0, pivot);
		List<SessionResult> later = ascending.subList(pivot, ascending.size());
		if (later.isEmpty()) {
			return null;
		}
		double delta = later.stream().mapToDouble(SessionResult::getScore).average().orElse(0)
				- earlier.stream().mapToDouble(SessionResult::getScore).average().orElse(0);
		return round(delta);
	}

	private Double averageScore(List<SessionResult> results) {
		if (results == null || results.isEmpty()) {
			return null;
		}
		return round(results.stream().mapToDouble(SessionResult::getScore).average().orElse(0));
	}

	private UserProfileResponse toUserProfileResponse(User user) {
		return new UserProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				resolveNickname(user),
				user.getLevel()
		);
	}

	private UserSettingsResponse toSettingsResponse(UserSettings settings) {
		return new UserSettingsResponse(
				settings.isDailyReminderEnabled(),
				settings.isSoundEffectsEnabled(),
				settings.isMouthGuideOverlayEnabled(),
				settings.isAutoPlayPronunciationEnabled(),
				settings.getPreferredCoachTone(),
				settings.getUpdatedAt()
		);
	}

	private static String resolveNickname(User user) {
		if (user.getNickname() != null && !user.getNickname().isBlank()) {
			return user.getNickname();
		}
		return user.getName();
	}

	private static String normalizeEmail(String email) {
		return requireText(email, "이메일").trim().toLowerCase();
	}

	private static String normalizeCoachTone(String tone) {
		String normalized = requireText(tone, "preferredCoachTone").trim().toLowerCase();
		return switch (normalized) {
			case "soft", "balanced", "strict" -> normalized;
			default -> "balanced";
		};
	}

	private static String requireText(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(label + "는 필수입니다.");
		}
		return value;
	}

	private static Double round(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
