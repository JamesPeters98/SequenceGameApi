package com.jamesdpeters.SequenceGame.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Profile("staging")
public class StagingBearerTokenFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private final String expectedToken;

	public StagingBearerTokenFilter(@Value("${app.auth.bearer-token:}") String expectedToken) {
		this.expectedToken = expectedToken == null ? "" : expectedToken.trim();
		if (this.expectedToken.isEmpty()) {
			throw new IllegalStateException("app.auth.bearer-token must be set when the staging profile is active");
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
				throws ServletException, IOException {
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			unauthorized(response);
			return;
		}

		var providedToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
		if (!constantTimeEquals(expectedToken, providedToken)) {
			unauthorized(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private static void unauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"error\":\"Unauthorized\"}");
	}

	private static boolean constantTimeEquals(String expected, String provided) {
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				provided.getBytes(StandardCharsets.UTF_8)
		);
	}
}
