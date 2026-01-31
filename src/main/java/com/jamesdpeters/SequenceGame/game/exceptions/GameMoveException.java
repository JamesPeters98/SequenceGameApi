package com.jamesdpeters.SequenceGame.game.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class GameMoveException extends ErrorResponseException {

	@Getter private final GameMoveError error;

	public GameMoveException(GameMoveError error) {
		var problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problemDetail.setTitle("Game move error");
		problemDetail.setDetail(error.message);
		problemDetail.setProperty("error", error.name());
		this.error = error;
		super(HttpStatus.FORBIDDEN, problemDetail, null);
	}

	@RequiredArgsConstructor
	public enum GameMoveError {
		GAME_NOT_IN_PROGRESS("Game not in progress"),
		NOT_YOUR_TURN("Player acted out of turn"),
		CARD_NOT_IN_HAND("Card not in hand"),
		CARD_NOT_PROVIDED("Card not provided"),
		INVALID_BOARD_POSITION("Invalid board position"),
		POSITION_OCCUPIED("Position already occupied"),
		CANNOT_REMOVE_SEQUENCE("Chip already part of a sequence"),
		CANNOT_REMOVE_EMPTY_CHIP("Cannot remove empty chip"),
		CANNOT_REMOVE_OWN_CHIP("Cannot remove own chip"),
		CANNOT_PLAY_ON_WILDCARD("Cannot play on wildcard"),
		PLAYER_NOT_FOUND("Player not found");

		private final String message;

	}
}
