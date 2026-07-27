USE pronunciation;

ALTER TABLE quiz_questions
ADD COLUMN IF NOT EXISTS iot_action_code VARCHAR(50) NULL;

UPDATE quiz_questions
SET iot_action_code = 'LIGHT_OFF'
WHERE sentence = 'Please turn off the ______.'
  AND LOWER(answer) = 'light';

UPDATE quiz_questions
SET iot_action_code = 'LIGHT_ON'
WHERE sentence = 'The room is too dark without the ______.'
  AND LOWER(answer) = 'light';

UPDATE quiz_questions
SET iot_action_code = 'FAN_ON'
WHERE sentence = 'Turn on the ______, please.'
  AND LOWER(answer) = 'fan';

SELECT
  id,
  sentence,
  answer,
  iot_action_code
FROM quiz_questions
WHERE iot_action_code IS NOT NULL
ORDER BY id;
