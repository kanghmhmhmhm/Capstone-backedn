package com.capstone.pronunciation.domain.curriculum.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;

@Configuration
public class CurriculumSeed {

	private static final String SENTENCE_STAGE_PREFIX = "Sentence Lv";
	private static final String BASIC_REFERENCE_STAGE_NAME = "REFERENCE_BASIC_PRONUNCIATION";
	private static final String WORD_REFERENCE_STAGE_NAME = "REFERENCE_WORDS";
	private static final String THINK_ANIMATION = """
			{"timedPhones":[{"phone":"TH","startMs":0,"endMs":120},{"phone":"IH1","startMs":120,"endMs":260},{"phone":"NG","startMs":260,"endMs":420},{"phone":"K","startMs":420,"endMs":480}],"totalDurationMs":480}
			""";
	private static final List<String> WORD_CHOICES = List.of(
			"rice", "light", "really", "glass", "fan", "phone", "coffee", "very", "vest", "think",
			"three", "thank", "mother", "she", "sheep", "shoes", "zoo", "water", "window", "apple",
			"cat", "bed", "milk", "sing", "choice", "girl", "world", "map", "spring"
	);

	@Bean
	ApplicationRunner seedCurriculum(CurriculumStageRepository stageRepository, QuizQuestionRepository questionRepository) {
		return args -> seedSentenceCurriculum(stageRepository, questionRepository);
	}

	private void seedSentenceCurriculum(
			CurriculumStageRepository stageRepository,
			QuizQuestionRepository questionRepository) {
		preserveReferenceStages(stageRepository);
		List<CurriculumStage> stages = new ArrayList<>();
		for (int level = 1; level <= 15; level++) {
			stages.add(upsertStage(stageRepository, SENTENCE_STAGE_PREFIX + level, level, level));
		}

		List<QuestionSeed> seeds = questionSeeds(stages);
		for (QuestionSeed seed : seeds) {
			upsertQuestion(questionRepository, seed);
		}
		deleteLegacySentenceQuestions(questionRepository, seeds);
		removeUnusedLegacyStage(stageRepository, "LEGACY_SENTENCE");
	}

	private void preserveReferenceStages(CurriculumStageRepository stageRepository) {
		renameStageIfPresent(stageRepository, "BASIC_PRONUNCIATION", BASIC_REFERENCE_STAGE_NAME, 901, 1);
		renameStageIfPresent(stageRepository, "WORD", WORD_REFERENCE_STAGE_NAME, 902, 2);
	}

	private CurriculumStage upsertStage(
			CurriculumStageRepository stageRepository,
			String stageName,
			int order,
			int difficulty) {
		CurriculumStage stage = stageRepository.findByStageNameIgnoreCase(stageName)
				.orElseGet(() -> new CurriculumStage(stageName, order, difficulty));
		boolean changed = false;
		if (stage.getOrder() != order) {
			stage.setOrder(order);
			changed = true;
		}
		if (stage.getDifficulty() != difficulty) {
			stage.setDifficulty(difficulty);
			changed = true;
		}
		if (stage.getId() == null || changed) {
			return stageRepository.save(stage);
		}
		return stage;
	}

	private void renameStageIfPresent(
			CurriculumStageRepository stageRepository,
			String oldName,
			String newName,
			int order,
			int difficulty) {
		CurriculumStage stage = stageRepository.findByStageNameIgnoreCase(oldName)
				.orElse(null);
		if (stage == null) {
			return;
		}
		stage.setStageName(newName);
		stage.setOrder(order);
		stage.setDifficulty(difficulty);
		stageRepository.save(stage);
	}

	private void upsertQuestion(QuizQuestionRepository questionRepository, QuestionSeed seed) {
		QuizQuestion question = questionRepository.findAll().stream()
				.filter(existing -> seed.sentence().equals(existing.getSentence())
						&& seed.answer().equalsIgnoreCase(existing.getAnswer() == null ? "" : existing.getAnswer()))
				.findFirst()
				.orElseGet(() -> new QuizQuestion(seed.stage(), seed.sentence(), seed.answer()));

		question.setStage(seed.stage());
		question.setAnswer(seed.answer());
		question.setPhoneticSymbol(seed.phoneticSymbol());
		question.setDifficulty(seed.difficulty());
		question.setChoiceOptions(choicesFor(seed.answer()));
		if ("think".equalsIgnoreCase(seed.answer())) {
			question.setAnimationData(THINK_ANIMATION);
		}
		questionRepository.save(question);
	}

	private void deleteLegacySentenceQuestions(
			QuizQuestionRepository questionRepository,
			List<QuestionSeed> seeds) {
		Set<String> activeKeys = new LinkedHashSet<>();
		for (QuestionSeed seed : seeds) {
			activeKeys.add(questionKey(seed.stage().getStageName(), seed.sentence(), seed.answer()));
		}

		for (QuizQuestion question : questionRepository.findByStage_StageNameStartingWithIgnoreCaseOrderByIdAsc(SENTENCE_STAGE_PREFIX)) {
			String key = questionKey(
					question.getStage().getStageName(),
					question.getSentence(),
					question.getAnswer()
			);
			if (!activeKeys.contains(key)) {
				questionRepository.deleteIfUnused(question.getId());
			}
		}
	}

	private void removeUnusedLegacyStage(CurriculumStageRepository stageRepository, String stageName) {
		CurriculumStage stage = stageRepository.findByStageNameIgnoreCase(stageName)
				.orElse(null);
		if (stage != null) {
			stageRepository.delete(stage);
		}
	}

	private String questionKey(String stageName, String sentence, String answer) {
		return "%s||%s||%s".formatted(
				stageName == null ? "" : stageName.trim().toLowerCase(),
				sentence == null ? "" : sentence.trim(),
				answer == null ? "" : answer.trim().toLowerCase()
		);
	}

	private List<String> choicesFor(String answer) {
		LinkedHashSet<String> choices = new LinkedHashSet<>();
		choices.add(answer);
		int start = Math.max(0, WORD_CHOICES.indexOf(answer));
		for (int offset = 1; choices.size() < 5 && offset <= WORD_CHOICES.size(); offset++) {
			choices.add(WORD_CHOICES.get((start + offset) % WORD_CHOICES.size()));
		}
		List<String> ordered = new ArrayList<>(choices);
		int rotation = Math.floorMod(answer.hashCode(), ordered.size());
		java.util.Collections.rotate(ordered, rotation);
		return ordered;
	}

	private List<QuestionSeed> questionSeeds(List<CurriculumStage> stages) {
		List<QuestionSeed> seeds = new ArrayList<>();
		List<WordSeed> words = wordSeeds();
		for (int wordIndex = 0; wordIndex < words.size(); wordIndex++) {
			WordSeed word = words.get(wordIndex);
			for (int sentenceIndex = 0; sentenceIndex < word.sentences().size(); sentenceIndex++) {
				CurriculumStage stage = stages.get((wordIndex + sentenceIndex * 3) % stages.size());
				seeds.add(new QuestionSeed(
						stage,
						word.sentences().get(sentenceIndex),
						word.answer(),
						word.phoneticSymbol(),
						stage.getDifficulty()
				));
			}
		}
		return seeds;
	}

	private List<WordSeed> wordSeeds() {
		return List.of(
				new WordSeed("rice", "/r/", List.of(
						"I eat ______ every day.",
						"We ordered fried ______ for dinner.",
						"My mom cooks brown ______ at home.",
						"The bowl is full of ______.",
						"I like chicken and ______.")),
				new WordSeed("light", "/l/", List.of(
						"Please turn off the ______.",
						"The room is too dark without the ______.",
						"I bought a new desk ______.",
						"The traffic ______ turned green.",
						"The kitchen ______ is very bright.")),
				new WordSeed("really", "/r/", List.of(
						"I ______ like this song.",
						"Are you ______ serious?",
						"That movie was ______ fun.",
						"I ______ want to go there.",
						"She is ______ good at English.")),
				new WordSeed("glass", "/gl/", List.of(
						"Please bring me a ______ of water.",
						"The window is made of ______.",
						"I dropped the ______ on the floor.",
						"This table has a ______ top.",
						"Be careful with the broken ______.")),
				new WordSeed("fan", "/f/", List.of(
						"Turn on the ______, please.",
						"I use a ______ in summer.",
						"The ceiling ______ is spinning.",
						"My room gets hot without the ______.",
						"She bought a portable ______.")),
				new WordSeed("phone", "/f/", List.of(
						"My ______ is ringing.",
						"I forgot my ______ at home.",
						"Please answer the ______.",
						"I use my ______ every morning.",
						"He dropped his ______ yesterday.")),
				new WordSeed("coffee", "/f/", List.of(
						"I drink ______ every morning.",
						"This café sells good ______.",
						"Would you like some ______?",
						"The smell of ______ is nice.",
						"She ordered iced ______.")),
				new WordSeed("very", "/v/", List.of(
						"This problem is ______ difficult.",
						"She runs ______ fast.",
						"I am ______ tired today.",
						"The food was ______ delicious.",
						"He is ______ kind to everyone.")),
				new WordSeed("vest", "/v/", List.of(
						"He wore a black ______.",
						"The suit comes with a ______.",
						"She bought a warm winter ______.",
						"The safety ______ is bright orange.",
						"My grandfather likes wearing a ______.")),
				new WordSeed("think", "/θ/", List.of(
						"I ______ this is correct.",
						"What do you ______ about it?",
						"Please ______ carefully.",
						"I don’t ______ he knows.",
						"We should ______ before acting.")),
				new WordSeed("three", "/θr/", List.of(
						"I have ______ brothers.",
						"There are ______ books on the table.",
						"She bought ______ apples.",
						"The movie starts in ______ minutes.",
						"We need ______ chairs.")),
				new WordSeed("thank", "/θ/", List.of(
						"I want to ______ you.",
						"Please ______ your teacher.",
						"We should ______ everyone.",
						"Don’t forget to say ______ you.",
						"I called to ______ him.")),
				new WordSeed("mother", "/ð/", List.of(
						"My ______ cooks well.",
						"I went shopping with my ______.",
						"Her ______ works at a hospital.",
						"My ______ likes flowers.",
						"I called my ______ yesterday.")),
				new WordSeed("she", "/ʃ/", List.of(
						"______ is my best friend.",
						"______ loves music.",
						"______ goes to school early.",
						"______ bought a new bag.",
						"______ speaks English well.")),
				new WordSeed("sheep", "/ʃ/", List.of(
						"The farmer has many ______.",
						"We saw a white ______.",
						"The ______ is eating grass.",
						"A ______ crossed the road.",
						"The little ______ looks cute.")),
				new WordSeed("shoes", "/ʃ/", List.of(
						"I bought new ______.",
						"Her ______ are dirty.",
						"Please take off your ______.",
						"These ______ are comfortable.",
						"He wears black ______.")),
				new WordSeed("zoo", "/z/", List.of(
						"We went to the ______.",
						"I saw lions at the ______.",
						"The ______ is crowded today.",
						"Children love visiting the ______.",
						"The new panda arrived at the ______.")),
				new WordSeed("water", "/w/", List.of(
						"Please give me some ______.",
						"I drink a lot of ______.",
						"The bottle is full of ______.",
						"Plants need ______ to grow.",
						"Cold ______ tastes good today.")),
				new WordSeed("window", "/w/", List.of(
						"Please open the ______.",
						"The ______ is very clean.",
						"I looked outside the ______.",
						"Rain hit the ______ loudly.",
						"She closed the ______ before sleeping.")),
				new WordSeed("apple", "/æ/", List.of(
						"I ate an ______.",
						"The red ______ looks fresh.",
						"She bought an ______ pie.",
						"An ______ fell from the tree.",
						"I packed an ______ for lunch.")),
				new WordSeed("cat", "/k/", List.of(
						"The ______ is sleeping.",
						"I have a black ______.",
						"The ______ climbed the tree.",
						"Her ______ likes fish.",
						"The little ______ is cute.")),
				new WordSeed("bed", "/b/", List.of(
						"I went to ______ early.",
						"My ______ is very soft.",
						"The dog jumped on the ______.",
						"She sat on the ______.",
						"I cleaned my ______ this morning.")),
				new WordSeed("milk", "/m/", List.of(
						"I drink ______ every morning.",
						"The baby needs ______.",
						"Please buy some ______.",
						"She poured ______ into the glass.",
						"Chocolate ______ is my favorite.")),
				new WordSeed("sing", "/ŋ/", List.of(
						"I love to ______.",
						"Can you ______ this song?",
						"We will ______ together.",
						"She can ______ very well.",
						"They ______ at church every Sunday.")),
				new WordSeed("phone", "/f/", List.of(
						"I checked my ______.",
						"Her ______ battery is dead.",
						"Please charge your ______.",
						"My ______ fell into the water.",
						"I bought a new ______ case.")),
				new WordSeed("choice", "/tʃ/", List.of(
						"That was a good ______.",
						"You have no other ______.",
						"It is your ______ to decide.",
						"She made the right ______.",
						"We respected his ______.")),
				new WordSeed("girl", "/ɝ/", List.of(
						"The ______ is reading a book.",
						"I met a friendly ______.",
						"That ______ is my cousin.",
						"The little ______ smiled at me.",
						"A ______ won the contest.")),
				new WordSeed("world", "/w/", List.of(
						"The ______ is changing fast.",
						"I want to travel around the ______.",
						"English is spoken worldwide around the ______.",
						"The news shocked the whole ______.",
						"She dreams of seeing the ______.")),
				new WordSeed("map", "/m/", List.of(
						"Please check the ______.",
						"I opened the city ______.",
						"The treasure is on the ______.",
						"We used a ______ while hiking.",
						"The subway ______ is easy to read.")),
				new WordSeed("spring", "/spr/", List.of(
						"Flowers bloom in ______.",
						"I like warm ______ weather.",
						"We traveled during ______ break.",
						"______ comes after winter.",
						"Many festivals happen in ______."))
		);
	}

	private record WordSeed(
			String answer,
			String phoneticSymbol,
			List<String> sentences
	) {
	}

	private record QuestionSeed(
			CurriculumStage stage,
			String sentence,
			String answer,
			String phoneticSymbol,
			int difficulty
	) {
	}
}
