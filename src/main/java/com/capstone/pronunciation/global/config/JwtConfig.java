package com.capstone.pronunciation.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtConfig {

	@Value("${app.jwt.secret:CHANGE_ME_DEV_SECRET_32BYTES_MINIMUM}")
	private String secret;

	@Value("${app.jwt.access-token-ttl-seconds:3600}")
	private long accessTokenTtlSeconds;

	@PostConstruct
	void init() {
		JwtUtil.configure(secret, accessTokenTtlSeconds);
	}
}
