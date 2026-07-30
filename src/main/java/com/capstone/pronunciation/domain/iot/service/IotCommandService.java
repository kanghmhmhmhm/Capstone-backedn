package com.capstone.pronunciation.domain.iot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.capstone.pronunciation.domain.iot.dto.IotCommandResponse;
import com.capstone.pronunciation.domain.iot.model.IotActionCode;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

@Service
public class IotCommandService {

	private final IotMqttPublisher mqttPublisher;
	private final double minimumScore;

	public IotCommandService(
			IotMqttPublisher mqttPublisher,
			@Value("${app.iot.minimum-score:7.0}") double minimumScore) {
		this.mqttPublisher = mqttPublisher;
		this.minimumScore = minimumScore;
	}

	public IotCommandResponse sendManualCommand(String actionCode) {
		return mqttPublisher.publish(IotActionCode.from(actionCode));
	}

	public IotCommandResponse dispatchAfterAnalysis(
			QuizQuestion question,
			String selectedChoice,
			double attemptAudioScore) {
		String actionCode = question.getIotActionCode();
		if (actionCode == null || actionCode.isBlank()) {
			return IotCommandResponse.skipped(null, "NOT_CONFIGURED", "해당 문제에는 IoT 명령이 설정되지 않았습니다.");
		}
		if (question.getAnswer() == null
				|| selectedChoice == null
				|| !question.getAnswer().trim().equalsIgnoreCase(selectedChoice.trim())) {
			return IotCommandResponse.skipped(actionCode, "INCORRECT_CHOICE", "정답을 선택하지 않아 IoT 명령을 실행하지 않았습니다.");
		}
		if (attemptAudioScore < minimumScore) {
			return IotCommandResponse.skipped(
					actionCode,
					"SCORE_TOO_LOW",
					"해당 시도의 음성 점수가 기준 점수 %.1f점보다 낮습니다.".formatted(minimumScore)
			);
		}

		try {
			return mqttPublisher.publish(IotActionCode.from(actionCode));
		} catch (IllegalArgumentException exception) {
			return IotCommandResponse.skipped(actionCode, "INVALID_ACTION", exception.getMessage());
		}
	}
}
