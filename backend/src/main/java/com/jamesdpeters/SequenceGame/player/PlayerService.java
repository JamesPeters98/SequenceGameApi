package com.jamesdpeters.SequenceGame.player;

import com.jamesdpeters.SequenceGame.game.Game;
import com.jamesdpeters.SequenceGame.game.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final GameRepository gameRepository;

	public List<Game> getGames(Player player) {
		return gameRepository.findByPlayer(player);
	}
}
