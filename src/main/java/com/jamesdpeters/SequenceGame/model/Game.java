package com.jamesdpeters.SequenceGame.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Game {

	private UUID uuid;
	private Status status;
	private final Instant createdDate = Instant.now();
	private Instant startedDate;
	private List<Player> players = new ArrayList<>();

	public Game(UUID uuid, Status status) {
		this.uuid = uuid;
		this.status = status;
	}

	public enum Status {
		NOT_STARTED, IN_PROGRESS, COMPLETED
	}
}
