package com.capstone.pronunciation.domain.ranking.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.ranking.dto.RankingEntryResponse;
import com.capstone.pronunciation.domain.ranking.dto.RankingResponse;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class RankingService {

	private final UserRepository userRepository;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;

	public RankingService(
			UserRepository userRepository,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository) {
		this.userRepository = userRepository;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
	}

	@Transactional(readOnly = true)
	public RankingResponse rankings(String email, int limit) {
		if (limit <= 0 || limit > 100) {
			throw new IllegalArgumentException("limit은 1~100 사이여야 합니다.");
		}

		User me = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		List<RankingEntryResponse> entries = buildEntries(me.getId());
		List<RankingEntryResponse> topEntries = entries.stream().limit(limit).toList();
		RankingEntryResponse myRank = entries.stream()
				.filter(RankingEntryResponse::me)
				.findFirst()
				.orElse(null);

		return new RankingResponse(Instant.now(), myRank, topEntries);
	}

	private List<RankingEntryResponse> buildEntries(Long currentUserId) {
		record Candidate(
				User user,
				double score,
				double averageScore,
				long solvedCount,
				long streak,
				String badge
		) {
		}

		List<Candidate> candidates = new ArrayList<>();
		for (User user : userRepository.findAll()) {
			List<SessionResult> results = sessionResultRepository.findDetailedByUserId(user.getId());
			if (results.isEmpty()) {
				continue;
			}
			List<LearningSession> sessions = learningSessionRepository.findByUser_IdOrderByStartTimeDesc(user.getId());
			double totalScore = round(results.stream().mapToDouble(SessionResult::getScore).sum());
			double averageScore = round(results.stream().mapToDouble(SessionResult::getScore).average().orElse(0));
			long streak = calculateSimpleStreak(sessions);
			candidates.add(new Candidate(
					user,
					totalScore,
					averageScore,
					results.size(),
					streak,
					resolveTopBadge(results, streak)
			));
		}

		candidates.sort(Comparator
				.comparingDouble(Candidate::score).reversed()
				.thenComparing(Comparator.comparingDouble(Candidate::averageScore).reversed())
				.thenComparing(Comparator.comparingLong(Candidate::solvedCount).reversed())
				.thenComparing(candidate -> resolveNickname(candidate.user())));

		List<RankingEntryResponse> rankings = new ArrayList<>();
		for (int i = 0; i < candidates.size(); i++) {
			Candidate candidate = candidates.get(i);
			rankings.add(new RankingEntryResponse(
					i + 1,
					candidate.user().getId(),
					resolveNickname(candidate.user()),
					candidate.user().getLevel(),
					candidate.score(),
					candidate.averageScore(),
					candidate.solvedCount(),
					candidate.badge(),
					avatarSeed(candidate.user()),
					candidate.user().getId().equals(currentUserId)
			));
		}
		return rankings;
	}

	private String resolveTopBadge(List<SessionResult> results, long streak) {
		Set<String> phonemes = results.stream()
				.map(result -> result.getQuestion().getPhoneticSymbol())
				.filter(symbol -> symbol != null && !symbol.isBlank())
				.collect(Collectors.toSet());
		double averageScore = results.stream().mapToDouble(SessionResult::getScore).average().orElse(0);
		if (averageScore >= 90.0 && results.size() >= 5) {
			return "Daily Master";
		}
		if (streak >= 3) {
			return "Consistent Learner";
		}
		if (phonemes.size() >= 5) {
			return "Phoneme Hunter";
		}
		if (results.size() >= 20) {
			return "Vocab Explorer";
		}
		return "Beginner Learner";
	}

	private long calculateSimpleStreak(List<LearningSession> sessions) {
		return sessions.stream()
				.map(LearningSession::getStartTime)
				.filter(startTime -> startTime != null)
				.map(startTime -> startTime.atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDate())
				.collect(Collectors.toSet())
				.size();
	}

	private static String resolveNickname(User user) {
		if (user.getNickname() != null && !user.getNickname().isBlank()) {
			return user.getNickname();
		}
		return user.getName();
	}

	private static String avatarSeed(User user) {
		String source = resolveNickname(user);
		return source == null || source.isBlank() ? "U" : source.substring(0, 1).toUpperCase();
	}

	private static double round(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
