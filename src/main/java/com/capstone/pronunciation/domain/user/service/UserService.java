package com.capstone.pronunciation.domain.user.service;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.user.dto.AuthResponse;
import com.capstone.pronunciation.domain.user.dto.DeleteAccountRequest;
import com.capstone.pronunciation.domain.user.dto.LoginRequest;
import com.capstone.pronunciation.domain.user.dto.MessageResponse;
import com.capstone.pronunciation.domain.user.dto.MyPageSummaryResponse;
import com.capstone.pronunciation.domain.user.dto.SignupRequest;
import com.capstone.pronunciation.domain.user.dto.UpdateProfileRequest;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.curriculum.repository.UserProgressRepository;
import com.capstone.pronunciation.domain.feedback.repository.FeedbackLogRepository;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.AnswerSubmissionRepository;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.PronunciationScoreRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.upload.repository.UploadFileRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;
import com.capstone.pronunciation.global.config.JwtUtil;
import com.capstone.pronunciation.global.exception.ConflictException;
import com.capstone.pronunciation.global.exception.UnauthorizedException;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final FeedbackLogRepository feedbackLogRepository;
	private final PronunciationScoreRepository pronunciationScoreRepository;
	private final AnswerSubmissionRepository answerSubmissionRepository;
	private final UserProgressRepository userProgressRepository;
	private final UploadFileRepository uploadFileRepository;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			FeedbackLogRepository feedbackLogRepository,
			PronunciationScoreRepository pronunciationScoreRepository,
			AnswerSubmissionRepository answerSubmissionRepository,
			UserProgressRepository userProgressRepository,
			UploadFileRepository uploadFileRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.feedbackLogRepository = feedbackLogRepository;
		this.pronunciationScoreRepository = pronunciationScoreRepository;
		this.answerSubmissionRepository = answerSubmissionRepository;
		this.userProgressRepository = userProgressRepository;
		this.uploadFileRepository = uploadFileRepository;
	}

	// 회원가입: id 중복 체크 -> 비밀번호 해시 -> 저장
	@Transactional
	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("이미 사용 중인 이메일입니다.");
		}

		String password = passwordEncoder.encode(requireText(request.password(), "비밀번호"));
		String name = requireText(request.name(), "이름");
		int level = 1;

		User user = new User(email, password, name, level);
		userRepository.save(user);

		String accessToken = JwtUtil.issueAccessToken(user.getEmail());
		return new AuthResponse(accessToken, new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getLevel()));
	}

	// 로그인: id 조회 -> 비밀번호 검증 -> 토큰 발급
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

		if (!passwordEncoder.matches(requireText(request.password(), "비밀번호"), user.getPassword())) {
			throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
		}

		String accessToken = JwtUtil.issueAccessToken(user.getEmail());
		return new AuthResponse(accessToken, new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getLevel()));
	}

	@Transactional(readOnly = true)
	public UserProfileResponse me(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("인증 정보가 올바르지 않습니다."));
		return new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getLevel());
	}

	@Transactional(readOnly = true)
	public MyPageSummaryResponse myPageSummary(String email) {
		User user = getUser(email);
		long totalSessions = learningSessionRepository.countByUser_Id(user.getId());
		long completedSessions = learningSessionRepository.countByUser_IdAndEndTimeIsNotNull(user.getId());
		var results = sessionResultRepository.findDetailedByUserId(user.getId());
		long totalSolvedQuestions = results.size();
		Double averageScore = results.isEmpty()
				? null
				: Math.round(results.stream().mapToInt(SessionResult::getScore).average().orElse(0) * 100.0) / 100.0;
		Instant lastStudiedAt = results.stream()
				.map(SessionResult::getCreatedAt)
				.max(Instant::compareTo)
				.orElse(null);

		return new MyPageSummaryResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				user.getLevel(),
				totalSessions,
				completedSessions,
				totalSolvedQuestions,
				averageScore,
				lastStudiedAt
		);
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

		boolean wantsPasswordChange = request.newPassword() != null && !request.newPassword().isBlank();
		if (wantsPasswordChange) {
			String currentPassword = requireText(request.currentPassword(), "현재 비밀번호");
			if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
				throw new UnauthorizedException("현재 비밀번호가 올바르지 않습니다.");
			}
			user.setPassword(passwordEncoder.encode(requireText(request.newPassword(), "새 비밀번호")));
		}

		return new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getLevel());
	}

	@Transactional(readOnly = true)
	public MessageResponse logout() {
		// JWT 기반 무상태 인증이라 서버 세션은 없고, 클라이언트가 토큰을 폐기하면 로그아웃이 완료된다.
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

	private static String normalizeEmail(String email) {
		return requireText(email, "이메일").trim().toLowerCase();
	}

	private static String requireText(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(label + "는 필수입니다.");
		}
		return value;
	}
}
