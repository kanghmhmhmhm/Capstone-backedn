-- Reset curriculum/question seed data to match local CurriculumSeed.java
-- Target DB: pronunciation
--
-- WARNING:
-- This deletes existing curriculum/question-related learning data.
-- Run only when you want EC2 seed data to match local development data.

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM feedback_logs;
DELETE FROM answer_submissions;
DELETE FROM pronunciation_scores;
DELETE FROM session_results;
DELETE FROM user_progress;
DELETE FROM quiz_questions;
DELETE FROM curriculum_stages;

ALTER TABLE feedback_logs AUTO_INCREMENT = 1;
ALTER TABLE answer_submissions AUTO_INCREMENT = 1;
ALTER TABLE pronunciation_scores AUTO_INCREMENT = 1;
ALTER TABLE session_results AUTO_INCREMENT = 1;
ALTER TABLE user_progress AUTO_INCREMENT = 1;
ALTER TABLE quiz_questions AUTO_INCREMENT = 1;
ALTER TABLE curriculum_stages AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO curriculum_stages (stage_name, stage_order, difficulty) VALUES
  ('ALPHABET', 1, 1),
  ('BASIC_PRONUNCIATION', 2, 2),
  ('WORD', 3, 3),
  ('Sentence Lv1', 4, 1),
  ('Sentence Lv2', 5, 2),
  ('Sentence Lv3', 6, 3),
  ('Sentence Lv4', 7, 4),
  ('Sentence Lv5', 8, 5);

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'A', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'B', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'C', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'D', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'E', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'F', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'G', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'H', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'J', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'K', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'L', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'M', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'N', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'O', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'P', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'Q', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'R', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'S', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'T', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'U', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'V', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'W', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'X', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'Y', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'Z', NULL, NULL, 1 FROM curriculum_stages WHERE stage_name = 'ALPHABET';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'sh', NULL, '/ʃ/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'th', NULL, '/θ/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ch', NULL, '/tʃ/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ph', NULL, '/f/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'wh', NULL, '/w/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ck', NULL, '/k/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ng', NULL, '/ŋ/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'qu', NULL, '/kw/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'wr', NULL, '/ɹ/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'kn', NULL, '/n/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'gn', NULL, '/n/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'mb', NULL, '/m/', 2 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'apples', NULL, NULL, 3 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'coffee', NULL, NULL, 3 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'water', NULL, NULL, 3 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'book', NULL, NULL, 3 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'school', NULL, NULL, 3 FROM curriculum_stages WHERE stage_name = 'WORD';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I see a ____.', 'cat', '/k/', 1 FROM curriculum_stages WHERE stage_name = 'Sentence Lv1';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is hot.', 'sun', '/s/', 1 FROM curriculum_stages WHERE stage_name = 'Sentence Lv1';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I have a ____.', 'book', '/b/', 1 FROM curriculum_stages WHERE stage_name = 'Sentence Lv1';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She likes ____.', 'milk', '/m/', 1 FROM curriculum_stages WHERE stage_name = 'Sentence Lv1';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'We ____ outside.', 'play', '/p/', 1 FROM curriculum_stages WHERE stage_name = 'Sentence Lv1';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is in the dish.', 'fish', '/f/', 2 FROM curriculum_stages WHERE stage_name = 'Sentence Lv2';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ my teeth every day.', 'brush', '/br/', 2 FROM curriculum_stages WHERE stage_name = 'Sentence Lv2';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is on the sea.', 'ship', '/ʃ/', 2 FROM curriculum_stages WHERE stage_name = 'Sentence Lv2';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'My ____ is small.', 'chin', '/tʃ/', 2 FROM curriculum_stages WHERE stage_name = 'Sentence Lv2';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is very fast.', 'van', '/v/', 2 FROM curriculum_stages WHERE stage_name = 'Sentence Lv2';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ this is my thumb.', 'think', '/θ/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, '____ is my brother.', 'This', '/ð/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She ____ a happy song.', 'sings', '/s/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is in the zoo.', 'zebra', '/z/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'He ____ over the box.', 'jumps', '/dʒ/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The girl heard the ____ early.', 'bird', '/ɜːr/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ to school with my friends.', 'walked', '/t/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She ____ the room yesterday.', 'cleaned', '/d/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'We ____ to watch a movie.', 'wanted', '/ɪd/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ shines in the night sky.', 'light', '/aɪ/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ the weather was smooth.', 'thought', '/θ/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ writes about culture.', 'author', '/ɔː/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She usually ____ the treasure.', 'measures', '/ʒ/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ runs quietly.', 'squirrel', '/skw/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'He succeeded ____ great effort.', 'through', '/θr/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
