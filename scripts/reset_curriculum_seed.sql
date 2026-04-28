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
ALTER TABLE quiz_questions ADD COLUMN IF NOT EXISTS choice_options LONGTEXT NULL;

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
SELECT id, 'teacher', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'window', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'banana', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'garden', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty)
SELECT id, 'pencil', NULL, NULL, 2 FROM curriculum_stages WHERE stage_name = 'WORD';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I see a ____.', 'cat', '/k/', 3, '["cat","dog","sun","book","fish"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is hot.', 'sun', '/s/', 3, '["moon","sun","book","milk","ship"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I have a ____.', 'book', '/b/', 3, '["fish","book","ship","chin","zebra"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'She likes ____.', 'milk', '/m/', 3, '["milk","play","van","cat","sun"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'We ____ outside.', 'play', '/p/', 3, '["play","think","brush","zebra","hold"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ can fly.', 'bird', '/b/', 3, '["bird","milk","ship","thumb","book"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I eat a red ____.', 'apple', '/æ/', 3, '["apple","zebra","play","fish","sun"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is blue.', 'sky', '/s/', 3, '["milk","sky","cat","book","van"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'He has a toy ____.', 'car', '/k/', 3, '["sun","car","ship","milk","brush"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'We sit on the ____.', 'chair', '/tʃ/', 3, '["zoo","chair","moon","book","van"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv3';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is in the dish.', 'fish', '/f/', 4, '["fish","ship","book","sun","milk"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I ____ my teeth every day.', 'brush', '/br/', 4, '["jumps","brush","sings","hold","think"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is on the sea.', 'ship', '/ʃ/', 4, '["ship","zebra","milk","chin","van"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'My ____ is small.', 'chin', '/tʃ/', 4, '["thumb","chin","teeth","book","fish"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is very fast.', 'van', '/v/', 4, '["cat","fish","van","sun","ship"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I wash my hands in the ____.', 'sink', '/s/', 4, '["sink","zebra","milk","thumb","fish"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is under the tree.', 'bench', '/b/', 4, '["brush","bench","ship","zebra","thumb"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'She wears a blue ____.', 'shirt', '/ʃ/', 4, '["ship","shirt","van","book","chair"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'We cross the old ____.', 'bridge', '/br/', 4, '["bridge","zebra","sings","milk","chair"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is full of stars.', 'night', '/n/', 4, '["night","thumb","fish","chair","ship"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv4';

INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'I ____ this is my thumb.', 'think', '/θ/', 5, '["take","think","stay","make","keep"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, '____ is my brother.', 'This', '/ð/', 5, '["That","This","These","Those","There"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'She ____ a happy song.', 'sings', '/s/', 5, '["sings","jumps","brush","thinks","holds"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The ____ is in the zoo.', 'zebra', '/z/', 5, '["zebra","ship","thumb","play","fish"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'He ____ over the box.', 'jumps', '/dʒ/', 5, '["jumps","sings","thinks","holds","brushes"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'Please ____ the door quietly.', 'close', '/k/', 5, '["close","brush","think","jumps","hold"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'They ____ to school by bus.', 'travel', '/tr/', 5, '["travel","sings","zebra","close","make"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'The baby is ____ on the bed.', 'sleeping', '/sl/', 5, '["sleeping","jumping","thinking","holding","taking"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'We ____ the answer together.', 'share', '/ʃ/', 5, '["share","close","travel","brush","think"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
INSERT INTO quiz_questions (stage_id, sentence, answer, phonetic_symbol, difficulty, choice_options)
SELECT id, 'Her voice sounds very ____.', 'gentle', '/dʒ/', 5, '["gentle","zebra","thumb","sleeping","close"]' FROM curriculum_stages WHERE stage_name = 'Sentence Lv5';
