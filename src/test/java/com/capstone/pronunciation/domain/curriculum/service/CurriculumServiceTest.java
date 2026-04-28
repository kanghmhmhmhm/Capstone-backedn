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
	private CurriculumStage basicPronunciationStage;
	private CurriculumStage wordStage;
	private CurriculumStage sentenceLv3Stage;
	private CurriculumStage sentenceLv4Stage;
	private QuizQuestion wordQuestion;
	private QuizQuestion sentenceLv3Question;
	private QuizQuestion sentenceLv4Question;

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User("learner@example.com", "encoded-password", "Learner", "Learner", 1));
		basicPronunciationStage = curriculumStageRepository.findByStageNameIgnoreCase("BASIC_PRONUNCIATION")
				.orElseThrow();
		wordStage = curriculumStageRepository.findByStageNameIgnoreCase("WORD")
				.orElseThrow();
		sentenceLv3Stage = curriculumStageRepository.findByStageNameIgnoreCase("Sentence Lv3")
				.orElseThrow();
		sentenceLv4Stage = curriculumStageRepository.findByStageNameIgnoreCase("Sentence Lv4")
				.orElseThrow();

		wordQuestion = quizQuestionRepository.findByStage_IdOrderByIdAsc(wordStage.getId()).stream()
				.findFirst()
				.orElseThrow();
		sentenceLv3Question = quizQuestionRepository.findByStage_IdOrderByIdAsc(sentenceLv3Stage.getId()).stream()
				.findFirst()
				.orElseThrow();
		sentenceLv4Question = quizQuestionRepository.findByStage_IdOrderByIdAsc(sentenceLv4Stage.getId()).stream()
				.findFirst()
				.orElseThrow();
	}

	@Test
	void stages_onlyUnlockNextStageAfterPreviousStageIsCompleted() {
		List<StageProgressResponse> beforeCompletion = curriculumService.stages(user.getEmail());

		assertStageUnlocked(beforeCompletion, "BASIC_PRONUNCIATION", true);
		assertStageUnlocked(beforeCompletion, "WORD", false);
		assertStageUnlocked(beforeCompletion, "Sentence Lv3", false);

		completeStage(basicPronunciationStage, 100.0);

		List<StageProgressResponse> afterCompletion = curriculumService.stages(user.getEmail());

		assertStageUnlocked(afterCompletion, "BASIC_PRONUNCIATION", true);
		assertStageUnlocked(afterCompletion, "WORD", true);
		assertStageUnlocked(afterCompletion, "Sentence Lv3", false);
	}

	@Test
	void lockedWordLevelCannotBeViewedOrCompleted() {
		IllegalArgumentException detailException = assertThrows(
				IllegalArgumentException.class,
				() -> curriculumService.questionDetail(user.getEmail(), wordQuestion.getId()));
		assertEquals("잠금 해제되지 않은 단계입니다.", detailException.getMessage());

		IllegalArgumentException completeException = assertThrows(
				IllegalArgumentException.class,
				() -> curriculumService.completeQuestion(user.getEmail(), wordQuestion.getId(), 100));
		assertEquals("잠금 해제되지 않은 단계입니다.", completeException.getMessage());
	}

	@Test
	void sentenceCategoryReturnsOnlyUnlockedSentenceLessons() {
		completeStage(basicPronunciationStage, 100.0);
		completeStage(wordStage, 100.0);

		List<Long> lessonIdsBeforeCompletion = curriculumService.lessonsByStageName(user.getEmail(), "SENTENCE").stream()
				.map(lesson -> lesson.id())
				.toList();

		assertTrue(lessonIdsBeforeCompletion.contains(sentenceLv3Question.getId()));
		assertFalse(lessonIdsBeforeCompletion.contains(sentenceLv4Question.getId()));

		completeStage(sentenceLv3Stage, 100.0);

		List<Long> lessonIdsAfterCompletion = curriculumService.lessonsByStageName(user.getEmail(), "SENTENCE").stream()
				.map(lesson -> lesson.id())
				.toList();

		assertTrue(lessonIdsAfterCompletion.contains(sentenceLv4Question.getId()));
	}

	private void complete(QuizQuestion question, double score) {
		LearningSession session = learningSessionRepository.save(
				new LearningSession(user, Instant.now(), Instant.now(), question.getDifficulty()));
		sessionResultRepository.save(new SessionResult(session, question, score));
	}

	private void completeStage(CurriculumStage stage, double score) {
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
