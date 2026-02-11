package com.jamesdpeters.SequenceGame.config;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String[] allowedOrigins;
	private final String[] allowedOriginPatterns;

	public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		var configuredValues = Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.map(origin -> origin.replaceAll("/+$", ""))
			.toList();

		this.allowedOrigins = configuredValues.stream()
			.filter(origin -> !origin.contains("*"))
			.toArray(String[]::new);
		this.allowedOriginPatterns = configuredValues.stream()
			.filter(origin -> origin.contains("*"))
			.toArray(String[]::new);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		var mapping = registry.addMapping("/**")
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			.allowedHeaders("*");

		if (allowedOrigins.length > 0) {
			mapping.allowedOrigins(allowedOrigins);
		}
		if (allowedOriginPatterns.length > 0) {
			mapping.allowedOriginPatterns(allowedOriginPatterns);
		}

		log.info("Configuring CORS origins={} patterns={}",
			String.join(", ", allowedOrigins),
			String.join(", ", allowedOriginPatterns));
	}
}
