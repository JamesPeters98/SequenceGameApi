package com.jamesdpeters.SequenceGame.config;

import com.jamesdpeters.SequenceGame.game.GameRepository;
import com.jamesdpeters.SequenceGame.game.InMemoryGameRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SequenceGameConfig {

	@Bean
	@Profile("inmemory")
	GameRepository gameRepository() {
		return new InMemoryGameRepository();
	}

}
