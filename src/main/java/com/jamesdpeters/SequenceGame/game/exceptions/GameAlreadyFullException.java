package com.jamesdpeters.SequenceGame.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class GameAlreadyFullException extends ErrorResponseException {
	public GameAlreadyFullException(UUID gameUuid, int maxPlayers) {
		var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Game " + gameUuid + " is full (max players: " + maxPlayers + ")");
		problemDetail.setTitle("Game full");
		problemDetail.setProperty("gameUuid", gameUuid);
		problemDetail.setProperty("maxPlayers", maxPlayers);

		super(HttpStatus.CONFLICT, problemDetail, null);
	}
}
