USE pronunciation;

ALTER TABLE quiz_questions
ADD COLUMN IF NOT EXISTS iot_action_code VARCHAR(50) NULL;

UPDATE quiz_questions
SET iot_action_code = NULL
WHERE iot_action_code IS NOT NULL
  AND iot_action_code NOT IN ('LIGHT_ON', 'LIGHT_OFF');

UPDATE quiz_questions
SET iot_action_code = 'LIGHT_OFF'
WHERE sentence = 'Please turn off the ______.'
  AND LOWER(answer) = 'light';

UPDATE quiz_questions
SET iot_action_code = 'LIGHT_ON'
WHERE sentence = 'The room is too dark without the ______.'
  AND LOWER(answer) = 'light';

UPDATE quiz_questions q
JOIN curriculum_stages s ON s.stage_name = 'Sentence Lv2'
SET q.stage_id = s.id,
    q.difficulty = s.difficulty
WHERE q.sentence = 'I eat ______ every day.'
  AND LOWER(q.answer) = 'rice';

UPDATE quiz_questions q
JOIN curriculum_stages s ON s.stage_name = 'Sentence Lv5'
SET q.stage_id = s.id,
    q.difficulty = s.difficulty
WHERE q.sentence = 'I bought new ______.'
  AND LOWER(q.answer) = 'shoes';

UPDATE quiz_questions q
JOIN curriculum_stages s ON s.stage_name = 'Sentence Lv1'
SET q.stage_id = s.id,
    q.difficulty = s.difficulty
WHERE (q.sentence = 'Please turn off the ______.' AND LOWER(q.answer) = 'light')
   OR (q.sentence = 'The room is too dark without the ______.' AND LOWER(q.answer) = 'light');

SELECT
  id,
  stage_id,
  sentence,
  answer,
  iot_action_code
FROM quiz_questions
WHERE iot_action_code IS NOT NULL
ORDER BY id;
