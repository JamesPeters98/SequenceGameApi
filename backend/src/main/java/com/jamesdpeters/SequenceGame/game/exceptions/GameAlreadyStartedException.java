package com.jamesdpeters.SequenceGame.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class GameAlreadyStartedException extends ErrorResponseException {
	public GameAlreadyStartedException(UUID uuid) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Game " + uuid + " has already started");
		problemDetail.setTitle("Game already started");
		problemDetail.setProperty("uuid", uuid);
		super(HttpStatus.FORBIDDEN, problemDetail, null);
	}
}
