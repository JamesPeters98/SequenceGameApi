package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.player.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

	@Test
	void onePlayerIsNotDealtCards() {
		Game game = new Game(1);
		game.addPlayer(new Player());
		assertThrows(IllegalStateException.class, game::dealCards);
	}

	@Test
	void cantAddToPlayersList() {
		Game game = new Game(2);
		assertThrows(UnsupportedOperationException.class, () -> game.getPlayers().add(UUID.randomUUID()));
	}

}