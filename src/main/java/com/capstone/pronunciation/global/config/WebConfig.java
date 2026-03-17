package com.capstone.pronunciation.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// SPA fallback for React Router deep links.
		// API controllers (/api/**) and other Spring endpoints take precedence over these view controllers.
		registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/index.html");
		registry.addViewController("/**/{path:[^\\.]*}").setViewName("forward:/index.html");
	}
}
