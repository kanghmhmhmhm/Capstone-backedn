package com.capstone.pronunciation.domain.curriculum.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
		List<StageProgressResponse> responses = new ArrayList<>(stages.size());

		for (CurriculumStage stage : stages) {
			long total = questionRepository.countByStage_Id(stage.getId());
			long completed = sessionResultRepository.countDistinctQuestionsByUserAndStage(user.getId(), stage.getId());
			boolean unlocked = true;
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

		CurriculumStage stage = stageRepository.findByStageName(stageName)
				.orElseThrow(() -> new IllegalArgumentException("단계를 찾을 수 없습니다."));

		List<QuizQuestion> questions = questionRepository.findByStage_IdOrderByIdAsc(stage.getId());
		List<LessonSummaryResponse> responses = new ArrayList<>(questions.size());
		for (QuizQuestion q : questions) {
			boolean completed = sessionResultRepository.existsByUserAndQuestion(user.getId(), q.getId());
			responses.add(new LessonSummaryResponse(
					q.getId(),
					stage.getId(),
					stage.getStageName(),
					q.getSentence(),
					q.getPhoneticSymbol(),
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

		boolean completed = sessionResultRepository.existsByUserAndQuestion(user.getId(), questionId);
		return new LessonDetailResponse(
				q.getId(),
				q.getStage().getId(),
				q.getStage().getStageName(),
				q.getSentence(),
				q.getPhoneticSymbol(),
				q.getAnswer(),
				completed
		);
	}

	@Transactional
	public void completeQuestion(String email, Long questionId, int score) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		QuizQuestion q = questionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		if (sessionResultRepository.existsByUserAndQuestion(user.getId(), questionId)) {
			return;
		}

		Instant now = Instant.now();
		LearningSession session = learningSessionRepository.save(new LearningSession(user, now, now));
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
}
