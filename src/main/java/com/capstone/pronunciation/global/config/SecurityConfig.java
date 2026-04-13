package com.capstone.pronunciation.global.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.cors(cors -> {});
		http.httpBasic(basic -> basic.disable());
		http.formLogin(form -> form.disable());
		http.logout(logout -> logout.disable());
		http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.exceptionHandling(eh -> eh
				.authenticationEntryPoint((req, res, ex) -> {
					res.setStatus(401);
					res.setCharacterEncoding("UTF-8");
					res.setContentType("application/json");
					res.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}");
				})
				.accessDeniedHandler((req, res, ex) -> {
					res.setStatus(403);
					res.setCharacterEncoding("UTF-8");
					res.setContentType("application/json");
					res.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"권한이 없습니다.\"}");
				}));

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll());

		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}
}
