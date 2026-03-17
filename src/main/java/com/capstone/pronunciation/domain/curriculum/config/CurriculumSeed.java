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

	@Bean
	ApplicationRunner seedCurriculum(CurriculumStageRepository stageRepository, QuizQuestionRepository questionRepository) {
		return args -> {
			if (stageRepository.count() > 0) {
				return;
			}

			CurriculumStage alphabet = stageRepository.save(new CurriculumStage("ALPHABET", 1, 1));
			CurriculumStage basicPron = stageRepository.save(new CurriculumStage("BASIC_PRONUNCIATION", 2, 2));
			CurriculumStage word = stageRepository.save(new CurriculumStage("WORD", 3, 3));
			CurriculumStage sentence = stageRepository.save(new CurriculumStage("SENTENCE", 4, 4));

			for (char ch = 'A'; ch <= 'Z'; ch++) {
				String letter = String.valueOf(ch);
				questionRepository.save(new QuizQuestion(alphabet, letter, null));
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

			questionRepository.save(new QuizQuestion(sentence, "Nice to meet ___.", "you"));
			questionRepository.save(new QuizQuestion(sentence, "How are ___ today?", "you"));
			questionRepository.save(new QuizQuestion(sentence, "My name is ___.", "min"));
			questionRepository.save(new QuizQuestion(sentence, "Please sit ___.", "down"));
			questionRepository.save(new QuizQuestion(sentence, "Thank ___ very much.", "you"));
		};
	}
}
