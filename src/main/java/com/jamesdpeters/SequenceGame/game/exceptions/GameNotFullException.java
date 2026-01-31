package com.jamesdpeters.SequenceGame.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;
import java.util.UUID;

public class GameNotFullException extends ErrorResponseException {
	public GameNotFullException(UUID game) {
		var problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problemDetail.setTitle("Game not full");
		problemDetail.setDetail("Game " + game + " is not full");
		problemDetail.setProperty("uuid", game);

		super(HttpStatus.NOT_FOUND, problemDetail, null);
	}
}
