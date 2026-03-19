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
			CurriculumStage sentenceLv1 = stageRepository.save(new CurriculumStage("Sentence Lv1", 4, 1));
			CurriculumStage sentenceLv2 = stageRepository.save(new CurriculumStage("Sentence Lv2", 5, 2));
			CurriculumStage sentenceLv3 = stageRepository.save(new CurriculumStage("Sentence Lv3", 6, 3));
			CurriculumStage sentenceLv4 = stageRepository.save(new CurriculumStage("Sentence Lv4", 7, 4));
			CurriculumStage sentenceLv5 = stageRepository.save(new CurriculumStage("Sentence Lv5", 8, 5));

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

			questionRepository.save(new QuizQuestion(sentenceLv1, "I see a ____.", "cat", "/k/", 1));
			questionRepository.save(new QuizQuestion(sentenceLv1, "The ____ is hot.", "sun", "/s/", 1));
			questionRepository.save(new QuizQuestion(sentenceLv1, "I have a ____.", "book", "/b/", 1));
			questionRepository.save(new QuizQuestion(sentenceLv1, "She likes ____.", "milk", "/m/", 1));
			questionRepository.save(new QuizQuestion(sentenceLv1, "We ____ outside.", "play", "/p/", 1));

			questionRepository.save(new QuizQuestion(sentenceLv2, "The ____ is in the dish.", "fish", "/f/", 2));
			questionRepository.save(new QuizQuestion(sentenceLv2, "I ____ my teeth every day.", "brush", "/br/", 2));
			questionRepository.save(new QuizQuestion(sentenceLv2, "The ____ is on the sea.", "ship", "/ʃ/", 2));
			questionRepository.save(new QuizQuestion(sentenceLv2, "My ____ is small.", "chin", "/tʃ/", 2));
			questionRepository.save(new QuizQuestion(sentenceLv2, "The ____ is very fast.", "van", "/v/", 2));

			questionRepository.save(new QuizQuestion(sentenceLv3, "I ____ this is my thumb.", "think", "/θ/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "____ is my brother.", "This", "/ð/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "She ____ a happy song.", "sings", "/s/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "The ____ is in the zoo.", "zebra", "/z/", 3));
			questionRepository.save(new QuizQuestion(sentenceLv3, "He ____ over the box.", "jumps", "/dʒ/", 3));

			questionRepository.save(new QuizQuestion(sentenceLv4, "The girl heard the ____ early.", "bird", "/ɜːr/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "I ____ to school with my friends.", "walked", "/t/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "She ____ the room yesterday.", "cleaned", "/d/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "We ____ to watch a movie.", "wanted", "/ɪd/", 4));
			questionRepository.save(new QuizQuestion(sentenceLv4, "The ____ shines in the night sky.", "light", "/aɪ/", 4));

			questionRepository.save(new QuizQuestion(sentenceLv5, "I ____ the weather was smooth.", "thought", "/θ/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "The ____ writes about culture.", "author", "/ɔː/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "She usually ____ the treasure.", "measures", "/ʒ/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "The ____ runs quietly.", "squirrel", "/skw/", 5));
			questionRepository.save(new QuizQuestion(sentenceLv5, "He succeeded ____ great effort.", "through", "/θr/", 5));
		};
	}
}
