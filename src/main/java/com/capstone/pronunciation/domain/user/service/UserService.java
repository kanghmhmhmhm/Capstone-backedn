package com.capstone.pronunciation.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.user.dto.AuthResponse;
import com.capstone.pronunciation.domain.user.dto.LoginRequest;
import com.capstone.pronunciation.domain.user.dto.SignupRequest;
import com.capstone.pronunciation.domain.user.dto.UserProfileResponse;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;
import com.capstone.pronunciation.global.config.JwtUtil;
import com.capstone.pronunciation.global.exception.ConflictException;
import com.capstone.pronunciation.global.exception.UnauthorizedException;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
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
