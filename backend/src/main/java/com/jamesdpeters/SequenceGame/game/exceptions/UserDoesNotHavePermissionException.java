package com.jamesdpeters.SequenceGame.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class UserDoesNotHavePermissionException extends ErrorResponseException {
	public UserDoesNotHavePermissionException(UUID hostUuid) {
		var problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
		problemDetail.setDetail("User does not have permission to perform this action UUID: " + hostUuid);
		problemDetail.setTitle("User does not have permission to perform this action");
		problemDetail.setProperty("uuid", hostUuid);
		super(HttpStatus.UNAUTHORIZED, problemDetail, null);
	}
}
