package com.jamesdpeters.SequenceGame.game.player;

import com.jamesdpeters.SequenceGame.game.Game;
import com.jamesdpeters.SequenceGame.game.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final GameRepository gameRepository;

	public List<Game> getGames(Player player) {
		return gameRepository.findByPlayer(player);
	}
}
