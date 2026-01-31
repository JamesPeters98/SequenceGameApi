package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
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

	@ParameterizedTest
	@CsvSource({
			"3, 3",
			"4, 2",
			"6, 3",
			"8, 2",
			"9, 3",
			"10, 2",
			"12, 3"
	})
	void playersAreOrganisedIntoCorrectNumberOfTeams(int playerCount, int expectedTeams) {
			Game game = new Game(playerCount);

			for (int i = 0; i < playerCount; i++) {
					game.addPlayer(new Player());
			}

			game.organiseTeams();

			var teams = new HashSet<>(game.getTeams().values());
			assertEquals(expectedTeams, teams.size());
	}

	@Test
	void firstPlayerIsCurrentTurn() {
		Game game = new Game(2);
		game.addPlayer(new Player());
		game.addPlayer(new Player());
		game.organiseTeams();

		assertEquals(game.getPlayers().getFirst(), game.getCurrentPlayerTurn());
	}

}