package com.capstone.pronunciation.domain.iot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.capstone.pronunciation.domain.iot.dto.IotCommandResponse;
import com.capstone.pronunciation.domain.iot.model.IotActionCode;
import com.capstone.pronunciation.domain.quiz.entity.QuizQuestion;

class IotCommandServiceTest {

	private IotMqttPublisher mqttPublisher;
	private IotCommandService iotCommandService;

	@BeforeEach
	void setUp() {
		mqttPublisher = mock(IotMqttPublisher.class);
		iotCommandService = new IotCommandService(mqttPublisher, 7.0);
	}

	@Test
	void dispatchesConfiguredActionWhenChoiceAndScorePass() {
		QuizQuestion question = question("light", "LIGHT_ON");
		IotCommandResponse sent = IotCommandResponse.sent("LIGHT_ON", "sent");
		when(mqttPublisher.publish(IotActionCode.LIGHT_ON)).thenReturn(sent);

		IotCommandResponse response = iotCommandService.dispatchAfterAnalysis(question, "light", 7.0);

		assertThat(response).isEqualTo(sent);
		verify(mqttPublisher).publish(IotActionCode.LIGHT_ON);
	}

	@Test
	void skipsWhenQuestionHasNoAction() {
		QuizQuestion question = question("light", null);

		IotCommandResponse response = iotCommandService.dispatchAfterAnalysis(question, "light", 9.0);

		assertThat(response.status()).isEqualTo("NOT_CONFIGURED");
		assertThat(response.triggered()).isFalse();
		verifyNoInteractions(mqttPublisher);
	}

	@Test
	void skipsWhenSelectedChoiceIsIncorrect() {
		QuizQuestion question = question("light", "LIGHT_ON");

		IotCommandResponse response = iotCommandService.dispatchAfterAnalysis(question, "fan", 9.0);

		assertThat(response.status()).isEqualTo("INCORRECT_CHOICE");
		assertThat(response.triggered()).isFalse();
		verifyNoInteractions(mqttPublisher);
	}

	@Test
	void skipsWhenScoreIsBelowMinimum() {
		QuizQuestion question = question("light", "LIGHT_ON");

		IotCommandResponse response = iotCommandService.dispatchAfterAnalysis(question, "light", 6.9);

		assertThat(response.status()).isEqualTo("SCORE_TOO_LOW");
		assertThat(response.triggered()).isFalse();
		verifyNoInteractions(mqttPublisher);
	}

	@Test
	void rejectsActionOutsideAllowList() {
		QuizQuestion question = question("light", "DELETE_DEVICE");

		IotCommandResponse response = iotCommandService.dispatchAfterAnalysis(question, "light", 9.0);

		assertThat(response.status()).isEqualTo("INVALID_ACTION");
		assertThat(response.triggered()).isFalse();
		verifyNoInteractions(mqttPublisher);
	}

	private QuizQuestion question(String answer, String actionCode) {
		QuizQuestion question = mock(QuizQuestion.class);
		when(question.getAnswer()).thenReturn(answer);
		when(question.getIotActionCode()).thenReturn(actionCode);
		return question;
	}
}
