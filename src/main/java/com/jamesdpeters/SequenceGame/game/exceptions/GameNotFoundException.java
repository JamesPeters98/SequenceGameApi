package com.jamesdpeters.SequenceGame.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;
import java.util.UUID;

public class GameNotFoundException extends ErrorResponseException {
	public GameNotFoundException(UUID game) {
		var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Game not found: " + game);
		problemDetail.setTitle("Game not found");
		problemDetail.setProperties(Map.of("gameUuid", game));

		super(HttpStatus.NOT_FOUND, problemDetail, null);
	}
}
