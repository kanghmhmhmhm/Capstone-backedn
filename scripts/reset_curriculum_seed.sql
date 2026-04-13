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
  ('BASIC_PRONUNCIATION', 1, 1),
  ('WORD', 2, 2),
  ('Sentence Lv3', 3, 3),
  ('Sentence Lv4', 4, 4),
  ('Sentence Lv5', 5, 5),
  ('Sentence Lv6', 6, 6),
  ('Sentence Lv7', 7, 7),
  ('Sentence Lv8', 8, 8),
  ('Sentence Lv9', 9, 9),
  ('Sentence Lv10', 10, 10),
  ('Sentence Lv11', 11, 11),
  ('Sentence Lv12', 12, 12),
  ('Sentence Lv13', 13, 13),
  ('Sentence Lv14', 14, 14),
  ('Sentence Lv15', 15, 15);

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'sh', NULL, '/ʃ/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'th', NULL, '/θ/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ch', NULL, '/tʃ/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ph', NULL, '/f/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'wh', NULL, '/w/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ck', NULL, '/k/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'ng', NULL, '/ŋ/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'qu', NULL, '/kw/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'wr', NULL, '/ɹ/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'kn', NULL, '/n/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'gn', NULL, '/n/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'mb', NULL, '/m/', 1 FROM curriculum_stages WHERE stage_name = 'BASIC_PRONUNCIATION';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'apples', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'coffee', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'water', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'book', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'school', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I see a ____.', 'cat', '/k/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is hot.', 'sun', '/s/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I have a ____.', 'book', '/b/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She likes ____.', 'milk', '/m/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'We ____ outside.', 'play', '/p/', 3 FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is in the dish.', 'fish', '/f/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ my teeth every day.', 'brush', '/br/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is on the sea.', 'ship', '/ʃ/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'My ____ is small.', 'chin', '/tʃ/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is very fast.', 'van', '/v/', 4 FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'I ____ this is my thumb.', 'think', '/θ/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, '____ is my brother.', 'This', '/ð/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'She ____ a happy song.', 'sings', '/s/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'The ____ is in the zoo.', 'zebra', '/z/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'He ____ over the box.', 'jumps', '/dʒ/', 5 FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
