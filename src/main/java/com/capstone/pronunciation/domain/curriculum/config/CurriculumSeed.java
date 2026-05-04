package com.capstone.pronunciation.domain.curriculum.config;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.capstone.pronunciation.domain.curriculum.entity.CurriculumStage;
import com.capstone.pronunciation.domain.curriculum.repository.CurriculumStageRepository;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;
import com.capstone.pronunciation.domain.quiz.repository.QuizQuestionRepository;

@Configuration
public class CurriculumSeed {

	private static final String THINK_ANIMATION = """
			{"timedPhones":[{"phone":"TH","startMs":0,"endMs":120},{"phone":"IH1","startMs":120,"endMs":260},{"phone":"NG","startMs":260,"endMs":420},{"phone":"K","startMs":420,"endMs":480}],"totalDurationMs":480}
			""";

	@Bean
	ApplicationRunner seedCurriculum(CurriculumStageRepository stageRepository, QuizQuestionRepository questionRepository) {
		return args -> {
			if (stageRepository.count() > 0) {
				backfillQuestionMetadata(questionRepository);
				return;
			}

			CurriculumStage basicPron = stageRepository.save(new CurriculumStage("BASIC_PRONUNCIATION", 1, 1));
			CurriculumStage word = stageRepository.save(new CurriculumStage("WORD", 2, 2));
			CurriculumStage sentenceLv3 = stageRepository.save(new CurriculumStage("Sentence Lv3", 3, 3));
			CurriculumStage sentenceLv4 = stageRepository.save(new CurriculumStage("Sentence Lv4", 4, 4));
			CurriculumStage sentenceLv5 = stageRepository.save(new CurriculumStage("Sentence Lv5", 5, 5));
			for (int level = 6; level <= 15; level++) {
				stageRepository.save(new CurriculumStage("Sentence Lv" + level, level, level));
			}

			questionRepository.save(new QuizQuestion(basicPron, "sh", null, "/ʃ/"));
			questionRepository.save(new QuizQuestion(basicPron, "th", null, "/θ/"));
			questionRepository.save(new QuizQuestion(basicPron, "ch", null, "/tʃ/"));
			questionRepository.save(new QuizQuestion(basicPron, "ph", null, "/f/"));
			questionRepository.save(new QuizQuestion(basicPron, "wh", null, "/w/"));
			questionRepository.save(new QuizQuestion(basicPron, "ck", null, "/k/"));
			questionRepository.save(new QuizQuestion(basicPron, "ng", null, "/ŋ/"));
			questionRepository.save(new QuizQuestion(basicPron, "qu", null, "/kw/"));
			questionRepository.save(new QuizQuestion(basicPron, "wr", null, "/ɹ/"));
			questionRepository.save(new QuizQuestion(basicPron, "kn", null, "/n/"));
			questionRepository.save(new QuizQuestion(basicPron, "gn", null, "/n/"));
			questionRepository.save(new QuizQuestion(basicPron, "mb", null, "/m/"));

			questionRepository.save(new QuizQuestion(word, "apples", null));
			questionRepository.save(new QuizQuestion(word, "coffee", null));
			questionRepository.save(new QuizQuestion(word, "water", null));
			questionRepository.save(new QuizQuestion(word, "book", null));
			questionRepository.save(new QuizQuestion(word, "school", null));
			questionRepository.save(new QuizQuestion(word, "teacher", null));
			questionRepository.save(new QuizQuestion(word, "window", null));
			questionRepository.save(new QuizQuestion(word, "banana", null));
			questionRepository.save(new QuizQuestion(word, "garden", null));
			questionRepository.save(new QuizQuestion(word, "pencil", null));

			questionRepository.save(new QuizQuestion(sentenceLv3, "I see a ____.", "cat", "/k/", 3, List.of("cat", "dog", "sun", "book", "fish")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "The ____ is hot.", "sun", "/s/", 3, List.of("moon", "sun", "book", "milk", "ship")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "I have a ____.", "book", "/b/", 3, List.of("fish", "book", "ship", "chin", "zebra")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "She likes ____.", "milk", "/m/", 3, List.of("milk", "play", "van", "cat", "sun")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "We ____ outside.", "play", "/p/", 3, List.of("play", "think", "brush", "zebra", "hold")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "The ____ can fly.", "bird", "/b/", 3, List.of("bird", "milk", "ship", "thumb", "book")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "I eat a red ____.", "apple", "/æ/", 3, List.of("apple", "zebra", "play", "fish", "sun")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "The ____ is blue.", "sky", "/s/", 3, List.of("milk", "sky", "cat", "book", "van")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "He has a toy ____.", "car", "/k/", 3, List.of("sun", "car", "ship", "milk", "brush")));
			questionRepository.save(new QuizQuestion(sentenceLv3, "We sit on the ____.", "chair", "/tʃ/", 3, List.of("zoo", "chair", "moon", "book", "van")));

			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is in the dish.", "fish", "/f/", 4, List.of("fish", "ship", "book", "sun", "milk")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "I ____ my teeth every day.", "brush", "/br/", 4, List.of("jumps", "brush", "sings", "hold", "think")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is on the sea.", "ship", "/ʃ/", 4, List.of("ship", "zebra", "milk", "chin", "van")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "My ____ is small.", "chin", "/tʃ/", 4, List.of("thumb", "chin", "teeth", "book", "fish")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is very fast.", "van", "/v/", 4, List.of("cat", "fish", "van", "sun", "ship")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "I wash my hands in the ____.", "sink", "/s/", 4, List.of("sink", "zebra", "milk", "thumb", "fish")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is under the tree.", "bench", "/b/", 4, List.of("brush", "bench", "ship", "zebra", "thumb")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "She wears a blue ____.", "shirt", "/ʃ/", 4, List.of("ship", "shirt", "van", "book", "chair")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "We cross the old ____.", "bridge", "/br/", 4, List.of("bridge", "zebra", "sings", "milk", "chair")));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is full of stars.", "night", "/n/", 4, List.of("night", "thumb", "fish", "chair", "ship")));

			questionRepository.save(new QuizQuestion(sentenceLv5, "I ____ this is my thumb.", "think", "/θ/", THINK_ANIMATION, 5, List.of("take", "think", "stay", "make", "keep")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "____ is my brother.", "This", "/ð/", 5, List.of("That", "This", "These", "Those", "There")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "She ____ a happy song.", "sings", "/s/", 5, List.of("sings", "jumps", "brush", "thinks", "holds")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "The ____ is in the zoo.", "zebra", "/z/", 5, List.of("zebra", "ship", "thumb", "play", "fish")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "He ____ over the box.", "jumps", "/dʒ/", 5, List.of("jumps", "sings", "thinks", "holds", "brushes")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "Please ____ the door quietly.", "close", "/k/", 5, List.of("close", "brush", "think", "jumps", "hold")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "They ____ to school by bus.", "travel", "/tr/", 5, List.of("travel", "sings", "zebra", "close", "make")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "The baby is ____ on the bed.", "sleeping", "/sl/", 5, List.of("sleeping", "jumping", "thinking", "holding", "taking")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "We ____ the answer together.", "share", "/ʃ/", 5, List.of("share", "close", "travel", "brush", "think")));
			questionRepository.save(new QuizQuestion(sentenceLv5, "Her voice sounds very ____.", "gentle", "/dʒ/", 5, List.of("gentle", "zebra", "thumb", "sleeping", "close")));
		};
	}

	private void backfillQuestionMetadata(QuizQuestionRepository questionRepository) {
		List<QuizQuestion> updatedQuestions = new java.util.ArrayList<>();
		for (QuizQuestion question : questionRepository.findAll()) {
			if (applySeedMetadata(question)) {
				updatedQuestions.add(question);
			}
		}
		if (!updatedQuestions.isEmpty()) {
			questionRepository.saveAll(updatedQuestions);
		}
	}

	private boolean applySeedMetadata(QuizQuestion question) {
		if (question.getSentence() == null) {
			return false;
		}

		return switch (question.getSentence()) {
			case "I see a ____." -> apply(question, "cat", "/k/", 3, List.of("cat", "dog", "sun", "book", "fish"), null);
			case "The ____ is hot." -> apply(question, "sun", "/s/", 3, List.of("moon", "sun", "book", "milk", "ship"), null);
			case "I have a ____." -> apply(question, "book", "/b/", 3, List.of("fish", "book", "ship", "chin", "zebra"), null);
			case "She likes ____." -> apply(question, "milk", "/m/", 3, List.of("milk", "play", "van", "cat", "sun"), null);
			case "We ____ outside." -> apply(question, "play", "/p/", 3, List.of("play", "think", "brush", "zebra", "hold"), null);
			case "The ____ can fly." -> apply(question, "bird", "/b/", 3, List.of("bird", "milk", "ship", "thumb", "book"), null);
			case "I eat a red ____." -> apply(question, "apple", "/æ/", 3, List.of("apple", "zebra", "play", "fish", "sun"), null);
			case "The ____ is blue." -> apply(question, "sky", "/s/", 3, List.of("milk", "sky", "cat", "book", "van"), null);
			case "He has a toy ____." -> apply(question, "car", "/k/", 3, List.of("sun", "car", "ship", "milk", "brush"), null);
			case "We sit on the ____." -> apply(question, "chair", "/tʃ/", 3, List.of("zoo", "chair", "moon", "book", "van"), null);
			case "The ____ is in the dish." -> apply(question, "fish", "/f/", 4, List.of("fish", "ship", "book", "sun", "milk"), null);
			case "I ____ my teeth every day." -> apply(question, "brush", "/br/", 4, List.of("jumps", "brush", "sings", "hold", "think"), null);
			case "The ____ is on the sea." -> apply(question, "ship", "/ʃ/", 4, List.of("ship", "zebra", "milk", "chin", "van"), null);
			case "My ____ is small." -> apply(question, "chin", "/tʃ/", 4, List.of("thumb", "chin", "teeth", "book", "fish"), null);
			case "The ____ is very fast." -> apply(question, "van", "/v/", 4, List.of("cat", "fish", "van", "sun", "ship"), null);
			case "I wash my hands in the ____." -> apply(question, "sink", "/s/", 4, List.of("sink", "zebra", "milk", "thumb", "fish"), null);
			case "The ____ is under the tree." -> apply(question, "bench", "/b/", 4, List.of("brush", "bench", "ship", "zebra", "thumb"), null);
			case "She wears a blue ____." -> apply(question, "shirt", "/ʃ/", 4, List.of("ship", "shirt", "van", "book", "chair"), null);
			case "We cross the old ____." -> apply(question, "bridge", "/br/", 4, List.of("bridge", "zebra", "sings", "milk", "chair"), null);
			case "The ____ is full of stars." -> apply(question, "night", "/n/", 4, List.of("night", "thumb", "fish", "chair", "ship"), null);
			case "I ____ this is my thumb." -> apply(question, "think", "/θ/", 5, List.of("take", "think", "stay", "make", "keep"), THINK_ANIMATION);
			case "____ is my brother." -> apply(question, "This", "/ð/", 5, List.of("That", "This", "These", "Those", "There"), null);
			case "She ____ a happy song." -> apply(question, "sings", "/s/", 5, List.of("sings", "jumps", "brush", "thinks", "holds"), null);
			case "The ____ is in the zoo." -> apply(question, "zebra", "/z/", 5, List.of("zebra", "ship", "thumb", "play", "fish"), null);
			case "He ____ over the box." -> apply(question, "jumps", "/dʒ/", 5, List.of("jumps", "sings", "thinks", "holds", "brushes"), null);
			case "Please ____ the door quietly." -> apply(question, "close", "/k/", 5, List.of("close", "brush", "think", "jumps", "hold"), null);
			case "They ____ to school by bus." -> apply(question, "travel", "/tr/", 5, List.of("travel", "sings", "zebra", "close", "make"), null);
			case "The baby is ____ on the bed." -> apply(question, "sleeping", "/sl/", 5, List.of("sleeping", "jumping", "thinking", "holding", "taking"), null);
			case "We ____ the answer together." -> apply(question, "share", "/ʃ/", 5, List.of("share", "close", "travel", "brush", "think"), null);
			case "Her voice sounds very ____." -> apply(question, "gentle", "/dʒ/", 5, List.of("gentle", "zebra", "thumb", "sleeping", "close"), null);
			default -> false;
		};
	}

	private boolean apply(
			QuizQuestion question,
			String answer,
			String phoneticSymbol,
			int difficulty,
			List<String> choices,
			String animationData) {
		boolean changed = false;
		if ((question.getAnswer() == null || question.getAnswer().isBlank()) && answer != null) {
			question.setAnswer(answer);
			changed = true;
		}
		if ((question.getPhoneticSymbol() == null || question.getPhoneticSymbol().isBlank()) && phoneticSymbol != null) {
			question.setPhoneticSymbol(phoneticSymbol);
			changed = true;
		}
		if (question.getDifficulty() != difficulty) {
			question.setDifficulty(difficulty);
			changed = true;
		}
		if (question.getChoiceOptions().isEmpty() && choices != null && !choices.isEmpty()) {
			question.setChoiceOptions(choices);
			changed = true;
		}
		if ((question.getAnimationData() == null || question.getAnimationData().isBlank()) && animationData != null && !animationData.isBlank()) {
			question.setAnimationData(animationData);
			changed = true;
		}
		return changed;
	}
}
