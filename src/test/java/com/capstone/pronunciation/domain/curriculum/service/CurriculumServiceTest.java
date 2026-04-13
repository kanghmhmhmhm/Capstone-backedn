package com.capstone.pronunciation.domain.curriculum.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pronunciation.domain.curriculum.dto.StageProgressResponse;
import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;
import com.capstone.pronunciation.domain.session.entity.LearningSession;
import com.capstone.pronunciation.domain.session.entity.SessionResult;
import com.capstone.pronunciation.domain.session.repository.LearningSessionRepository;
import com.capstone.pronunciation.domain.session.repository.SessionResultRepository;
import com.capstone.pronunciation.domain.user.entity.User;
import com.capstone.pronunciation.domain.user.repository.UserRepository;

@SpringBootTest
@Transactional
class CurriculumServiceTest {

	@Autowired
	private CurriculumService curriculumService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CurriculumStageRepository curriculumStageRepository;

	@Autowired
	private QuizQuestionRepository quizQuestionRepository;

	@Autowired
	private LearningSessionRepository learningSessionRepository;

	@Autowired
	private SessionResultRepository sessionResultRepository;

	private User user;
	private CurriculumStage alphabetStage;
	private CurriculumStage sentenceLv1Stage;
	private CurriculumStage sentenceLv2Stage;
	private QuizQuestion sentenceLv1Question;
	private QuizQuestion sentenceLv2Question;

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User("learner@example.com", "encoded-password", "Learner", 1));
		alphabetStage = curriculumStageRepository.findByStageNameIgnoreCase("ALPHABET")
				.orElseThrow();
		sentenceLv1Stage = curriculumStageRepository.findByStageNameIgnoreCase("Sentence Lv1")
				.orElseThrow();
		sentenceLv2Stage = curriculumStageRepository.findByStageNameIgnoreCase("Sentence Lv2")
				.orElseThrow();

		sentenceLv1Question = quizQuestionRepository.findByStage_IdOrderByIdAsc(sentenceLv1Stage.getId()).stream()
				.findFirst()
				.orElseThrow();
		sentenceLv2Question = quizQuestionRepository.findByStage_IdOrderByIdAsc(sentenceLv2Stage.getId()).stream()
				.findFirst()
				.orElseThrow();
	}

	@Test
	void stages_onlyUnlockNextSentenceLevelAfterPreviousSentenceIsCompleted() {
		List<StageProgressResponse> beforeCompletion = curriculumService.stages(user.getEmail());

		assertStageUnlocked(beforeCompletion, "ALPHABET", true);
		assertStageUnlocked(beforeCompletion, "Sentence Lv1", true);
		assertStageUnlocked(beforeCompletion, "Sentence Lv2", false);

		completeStage(sentenceLv1Stage, 100);

		List<StageProgressResponse> afterCompletion = curriculumService.stages(user.getEmail());

		assertStageUnlocked(afterCompletion, "Sentence Lv1", true);
		assertStageUnlocked(afterCompletion, "Sentence Lv2", true);
	}

	@Test
	void lockedSentenceLevelCannotBeViewedOrCompleted() {
		IllegalArgumentException detailException = assertThrows(
				IllegalArgumentException.class,
				() -> curriculumService.questionDetail(user.getEmail(), sentenceLv2Question.getId()));
		assertEquals("잠금 해제되지 않은 단계입니다.", detailException.getMessage());

		IllegalArgumentException completeException = assertThrows(
				IllegalArgumentException.class,
				() -> curriculumService.completeQuestion(user.getEmail(), sentenceLv2Question.getId(), 100));
		assertEquals("잠금 해제되지 않은 단계입니다.", completeException.getMessage());
	}

	@Test
	void sentenceCategoryReturnsOnlyUnlockedSentenceLessons() {
		List<Long> lessonIdsBeforeCompletion = curriculumService.lessonsByStageName(user.getEmail(), "SENTENCE").stream()
				.map(lesson -> lesson.id())
				.toList();

		assertTrue(lessonIdsBeforeCompletion.contains(sentenceLv1Question.getId()));
		assertFalse(lessonIdsBeforeCompletion.contains(sentenceLv2Question.getId()));

		completeStage(sentenceLv1Stage, 100);

		List<Long> lessonIdsAfterCompletion = curriculumService.lessonsByStageName(user.getEmail(), "SENTENCE").stream()
				.map(lesson -> lesson.id())
				.toList();

		assertTrue(lessonIdsAfterCompletion.contains(sentenceLv2Question.getId()));
	}

	private void complete(QuizQuestion question, int score) {
		LearningSession session = learningSessionRepository.save(
				new LearningSession(user, Instant.now(), Instant.now(), question.getDifficulty()));
		sessionResultRepository.save(new SessionResult(session, question, score));
	}

	private void completeStage(CurriculumStage stage, int score) {
		for (QuizQuestion question : quizQuestionRepository.findByStage_IdOrderByIdAsc(stage.getId())) {
			complete(question, score);
		}
	}

	private void assertStageUnlocked(List<StageProgressResponse> stages, String stageName, boolean expected) {
		StageProgressResponse stage = stages.stream()
				.filter(item -> item.stageName().equals(stageName))
				.findFirst()
				.orElseThrow(() -> new AssertionError("stage not found: " + stageName));
		assertEquals(expected, stage.unlocked());
	}
}
