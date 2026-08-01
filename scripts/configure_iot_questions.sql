USE pronunciation;

ALTER TABLE quiz_questions
ADD COLUMN IF NOT EXISTS iot_action_code VARCHAR(50) NULL;

UPDATE quiz_questions
SET iot_action_code = CASE LOWER(answer)
  WHEN 'light on' THEN 'LIGHT_ON'
  WHEN 'light off' THEN 'LIGHT_OFF'
  WHEN 'red' THEN 'LIGHT_RED'
  WHEN 'green' THEN 'LIGHT_GREEN'
  WHEN 'blue' THEN 'LIGHT_BLUE'
  ELSE NULL
END;

SELECT
  id,
  stage_id,
  sentence,
  answer,
  iot_action_code
FROM quiz_questions
WHERE iot_action_code IS NOT NULL
ORDER BY id;
