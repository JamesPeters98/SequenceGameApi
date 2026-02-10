package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.persistence.mapper.GameToDomainMapper;
import com.jamesdpeters.SequenceGame.game.persistence.mapper.GameToEntityMapper;
import com.jamesdpeters.SequenceGame.game.persistence.repository.GameJpaRepository;
import com.jamesdpeters.SequenceGame.player.Player;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DbGameRepository implements GameRepository {

	private final GameJpaRepository gameJpaRepository;
	private final GameToEntityMapper entityMapper;
	private final GameToDomainMapper domainMapper;

	@Override
	@Transactional
	public Game save(Game game) {
		var existing = gameJpaRepository.findById(game.getUuid()).orElse(null);
		if (existing == null) {
			gameJpaRepository.save(entityMapper.toEntity(game));
			return game;
		}

		entityMapper.clearChildCollections(existing);
		gameJpaRepository.flush();
		entityMapper.toEntity(game, existing, false);
		gameJpaRepository.save(existing);
		return game;
	}

	@Override
	@Transactional
	public Game findByUuid(UUID uuid) {
		var entity = gameJpaRepository.findById(uuid).orElse(null);
		if (entity == null) {
			return null;
		}
		return domainMapper.toDomain(entity);
	}

	@Override
	@Transactional
	public List<Game> findByPlayer(Player player) {
		var entities = gameJpaRepository.findByPlayer(player);
		return entities.stream().map(domainMapper::toDomain).toList();
	}

	@Override
	@Transactional
	public Collection<Game> findAll() {
		var entities = gameJpaRepository.findAll();
		return entities.stream().map(domainMapper::toDomain).toList();
	}
}
