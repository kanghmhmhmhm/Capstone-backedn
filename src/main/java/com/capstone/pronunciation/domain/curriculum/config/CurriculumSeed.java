package com.capstone.pronunciation.domain.curriculum.config;

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

			questionRepository.save(new QuizQuestion(sentenceLv3, "I see a ____.", "cat", "/k/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "The ____ is hot.", "sun", "/s/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "I have a ____.", "book", "/b/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "She likes ____.", "milk", "/m/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "We ____ outside.", "play", "/p/", 3));

			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is in the dish.", "fish", "/f/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "I ____ my teeth every day.", "brush", "/br/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is on the sea.", "ship", "/ʃ/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "My ____ is small.", "chin", "/tʃ/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ is very fast.", "van", "/v/", 4));

			questionRepository.save(new QuizQuestion(sentenceLv5, "I ____ this is my thumb.", "think", "/θ/", THINK_ANIMATION, 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "____ is my brother.", "This", "/ð/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "She ____ a happy song.", "sings", "/s/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "The ____ is in the zoo.", "zebra", "/z/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "He ____ over the box.", "jumps", "/dʒ/", 5));
		};
	}
}
