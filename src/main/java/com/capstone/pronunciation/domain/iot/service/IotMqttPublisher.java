package com.capstone.pronunciation.domain.iot.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.capstone.pronunciation.domain.iot.dto.IotCommandResponse;
import com.capstone.pronunciation.domain.iot.model.IotActionCode;

import jakarta.annotation.PreDestroy;

@Service
public class IotMqttPublisher {

	private static final Logger log = LoggerFactory.getLogger(IotMqttPublisher.class);

	private final boolean enabled;
	private final String brokerUri;
	private final String commandTopic;
	private final String clientId;
	private MqttClient mqttClient;

	public IotMqttPublisher(
			@Value("${app.iot.enabled:false}") boolean enabled,
			@Value("${app.iot.mqtt-broker-uri:tcp://broker.hivemq.com:1883}") String brokerUri,
			@Value("${app.iot.mqtt-command-topic:pronimo/demo-20260725/device01/cmd}") String commandTopic,
			@Value("${app.iot.mqtt-client-id-prefix:pronimo-backend}") String clientIdPrefix) {
		this.enabled = enabled;
		this.brokerUri = brokerUri;
		this.commandTopic = commandTopic;
		this.clientId = clientIdPrefix + "-" + UUID.randomUUID();
	}

	public synchronized IotCommandResponse publish(IotActionCode actionCode) {
		if (!enabled) {
			return IotCommandResponse.skipped(
					actionCode.name(),
					"DISABLED",
					"IoT 연동이 비활성화되어 있습니다. IOT_ENABLED=true로 설정하세요."
			);
		}

		try {
			ensureConnected();
			MqttMessage message = new MqttMessage(actionCode.name().getBytes(StandardCharsets.UTF_8));
			message.setQos(1);
			message.setRetained(false);
			mqttClient.publish(commandTopic, message);
			log.info("IoT MQTT command sent: topic={}, actionCode={}", commandTopic, actionCode);
			return IotCommandResponse.sent(actionCode.name(), "IoT 명령을 전송했습니다.");
		} catch (MqttException exception) {
			log.warn(
					"IoT MQTT command failed: brokerUri={}, topic={}, actionCode={}",
					brokerUri,
					commandTopic,
					actionCode,
					exception
			);
			disconnectQuietly();
			return IotCommandResponse.failed(actionCode.name(), "MQTT 명령 전송에 실패했습니다.");
		}
	}

	private void ensureConnected() throws MqttException {
		if (mqttClient != null && mqttClient.isConnected()) {
			return;
		}

		disconnectQuietly();
		mqttClient = new MqttClient(brokerUri, clientId, new MemoryPersistence());
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(5);
		options.setKeepAliveInterval(30);
		mqttClient.connect(options);
		log.info("IoT MQTT connected: brokerUri={}, clientId={}", brokerUri, clientId);
	}

	@PreDestroy
	public synchronized void close() {
		disconnectQuietly();
	}

	private void disconnectQuietly() {
		if (mqttClient == null) {
			return;
		}
		try {
			if (mqttClient.isConnected()) {
				mqttClient.disconnect();
			}
			mqttClient.close();
		} catch (MqttException exception) {
			log.debug("IoT MQTT disconnect failed", exception);
		} finally {
			mqttClient = null;
		}
	}
}
