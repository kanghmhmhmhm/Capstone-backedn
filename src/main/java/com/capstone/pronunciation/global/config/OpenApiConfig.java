package com.capstone.pronunciation.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Capstone API")
						.description("""
								AI-based pronunciation learning platform API

								Swagger 표기 규칙
								- `Frontend - ...` 태그: 프론트엔드가 직접 호출하는 최종 API
								- `Support - ...` 태그: 로컬 테스트나 내부 검증용 보조 API
								- `[프론트 사용]`: 프론트엔드 호출 대상
								- `[AI 서버 연동]`: Spring 서버가 FastAPI AI 서버와 연동하는 API
								- `[보조용]`: 프론트 메인 흐름에서는 사용하지 않는 API
								- `[호환용]`: 기존 구조 호환을 위해 남겨둔 엔드포인트
								""")
						.version("v1"))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
	}
}
