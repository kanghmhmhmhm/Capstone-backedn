package com.capstone.pronunciation.domain.curriculum.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.dto.LessonDetailResponse;
import com.capstone.pronunciation.domain.curriculum.dto.LessonSummaryResponse;
import com.capstone.pronunciation.domain.curriculum.dto.StageProgressResponse;
import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.entity.UserProgress;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.curriculum.repository.UserProgressRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@Service
public class CurriculumService {
	private static final String SENTENCE_STAGE_PREFIX = "Sentence Lv";
	private static final String SENTENCE_STAGE_NAME = "sentence";

	private final CurriculumStageRepository stageRepository;
	private final QuizQuestionRepository questionRepository;
	private final LearningSessionRepository learningSessionRepository;
	private final SessionResultRepository sessionResultRepository;
	private final UserProgressRepository userProgressRepository;
	private final UserRepository userRepository;

	public CurriculumService(
			CurriculumStageRepository stageRepository,
			QuizQuestionRepository questionRepository,
			LearningSessionRepository learningSessionRepository,
			SessionResultRepository sessionResultRepository,
			UserProgressRepository userProgressRepository,
			UserRepository userRepository) {
		this.stageRepository = stageRepository;
		this.questionRepository = questionRepository;
		this.learningSessionRepository = learningSessionRepository;
		this.sessionResultRepository = sessionResultRepository;
		this.userProgressRepository = userProgressRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<StageProgressResponse> stages(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		List<CurriculumStage> stages = stageRepository.findAllByOrderByOrderAsc();
		Map<Long, Boolean> unlockedByStageId = buildUnlockedStageMap(user.getId(), stages);
		List<StageProgressResponse> responses = new ArrayList<>(stages.size());

		for (CurriculumStage stage : stages) {
			long total = questionRepository.countByStage_Id(stage.getId());
			long completed = sessionResultRepository.countDistinctQuestionsByUserAndStage(user.getId(), stage.getId());
			boolean unlocked = unlockedByStageId.getOrDefault(stage.getId(), true);
			boolean stageCompleted = total > 0 && completed >= total;

			responses.add(new StageProgressResponse(
					stage.getId(),
					stage.getStageName(),
					stage.getOrder(),
					stage.getDifficulty(),
					unlocked,
					stageCompleted,
					completed,
					total
			));
		}

		return responses;
	}

	@Transactional(readOnly = true)
	public List<LessonSummaryResponse> lessonsByStageName(String email, String stageName) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		List<CurriculumStage> stages = stageRepository.findAllByOrderByOrderAsc();
		Map<Long, Boolean> unlockedByStageId = buildUnlockedStageMap(user.getId(), stages);

		List<QuizQuestion> questions;
		if (isSentenceCategory(stageName)) {
			questions = questionRepository.findByStage_StageNameStartingWithIgnoreCaseOrderByIdAsc(SENTENCE_STAGE_PREFIX)
					.stream()
					.filter(question -> unlockedByStageId.getOrDefault(question.getStage().getId(), true))
					.toList();
		} else if (isSentenceStage(stageName)) {
			CurriculumStage stage = stageRepository.findByStageNameIgnoreCase(stageName)
					.orElseThrow(() -> new IllegalArgumentException("단계를 찾을 수 없습니다."));
			ensureUnlocked(stage, unlockedByStageId);
			questions = questionRepository.findByStage_IdOrderByIdAsc(stage.getId());
		} else {
			CurriculumStage stage = stageRepository.findByStageNameIgnoreCase(stageName)
					.orElseThrow(() -> new IllegalArgumentException("단계를 찾을 수 없습니다."));
			questions = questionRepository.findByStage_IdOrderByIdAsc(stage.getId());
		}

		List<LessonSummaryResponse> responses = new ArrayList<>(questions.size());
		for (QuizQuestion q : questions) {
			boolean completed = sessionResultRepository.existsByUserAndQuestion(user.getId(), q.getId());
			responses.add(new LessonSummaryResponse(
					q.getId(),
					q.getStage().getId(),
					q.getStage().getStageName(),
					q.getDifficulty(),
					q.getSentence(),
					q.getPhoneticSymbol(),
					q.getAnimationData(),
					completed
			));
		}
		return responses;
	}

	@Transactional(readOnly = true)
	public LessonDetailResponse questionDetail(String email, Long questionId) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
		ensureUnlocked(q.getStage(), buildUnlockedStageMap(
				user.getId(),
				stageRepository.findAllByOrderByOrderAsc()));

		boolean completed = sessionResultRepository.existsByUserAndQuestion(user.getId(), questionId);
		return new LessonDetailResponse(
				q.getId(),
				q.getStage().getId(),
				q.getStage().getStageName(),
				q.getDifficulty(),
				q.getSentence(),
				q.getPhoneticSymbol(),
				q.getAnswer(),
				q.getChoiceOptions(),
				q.getAnimationData(),
				completed
		);
	}

	@Transactional
	public void completeQuestion(String email, Long questionId, int score) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
		ensureUnlocked(q.getStage(), buildUnlockedStageMap(
				user.getId(),
				stageRepository.findAllByOrderByOrderAsc()));

		if (sessionResultRepository.existsByUserAndQuestion(user.getId(), questionId)) {
			return;
		}

		Instant now = Instant.now();
		LearningSession session = learningSessionRepository.save(new LearningSession(user, now, now, q.getDifficulty()));
		sessionResultRepository.save(new SessionResult(session, q, score));

		markStageCompletedIfDone(user, q.getStage());
	}

	private void markStageCompletedIfDone(User user, CurriculumStage stage) {
		long total = questionRepository.countByStage_Id(stage.getId());
		long completed = sessionResultRepository.countDistinctQuestionsByUserAndStage(user.getId(), stage.getId());
		if (total > 0 && completed >= total) {
			UserProgress progress = userProgressRepository.findByUser_IdAndStage_Id(user.getId(), stage.getId())
					.orElseGet(() -> new UserProgress(user, stage, false));
			progress.setCompleted(true);
			userProgressRepository.save(progress);
		}
	}

	private static boolean isSentenceStage(String stageName) {
		if (stageName == null) {
			return false;
		}
		String normalized = stageName.trim().toLowerCase();
		return normalized.equals("sentence") || normalized.startsWith("sentence lv");
	}

	private static boolean isSentenceCategory(String stageName) {
		return stageName != null && SENTENCE_STAGE_NAME.equals(stageName.trim().toLowerCase());
	}

	private Map<Long, Boolean> buildUnlockedStageMap(Long userId, List<CurriculumStage> stages) {
		Map<Long, Boolean> unlockedByStageId = new HashMap<>();
		boolean previousStageCompleted = true;
		boolean firstStage = true;

		for (CurriculumStage stage : stages) {
			long total = questionRepository.countByStage_Id(stage.getId());
			long completed = sessionResultRepository.countDistinctQuestionsByUserAndStage(userId, stage.getId());
			boolean stageCompleted = total > 0 && completed >= total;

			boolean unlocked = firstStage || previousStageCompleted;
			unlockedByStageId.put(stage.getId(), unlocked);

			firstStage = false;
			previousStageCompleted = stageCompleted;
		}

		return unlockedByStageId;
	}

	private void ensureUnlocked(CurriculumStage stage, Map<Long, Boolean> unlockedByStageId) {
		if (!unlockedByStageId.getOrDefault(stage.getId(), true)) {
			throw new IllegalArgumentException("잠금 해제되지 않은 단계입니다.");
		}
	}
}
