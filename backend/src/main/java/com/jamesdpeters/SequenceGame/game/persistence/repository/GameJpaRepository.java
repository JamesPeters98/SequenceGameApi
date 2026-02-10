package com.jamesdpeters.SequenceGame.game.persistence.repository;

import com.jamesdpeters.SequenceGame.game.persistence.entity.GameEntity;
import com.jamesdpeters.SequenceGame.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GameJpaRepository extends JpaRepository<GameEntity, UUID> {

	@Query("""
			select distinct g
			from GameEntity g
			join g.players p
			where p.publicUuid = :publicUuid
			""")
	List<GameEntity> findByPlayerPublicUuid(@Param("publicUuid") UUID publicUuid);

	default List<GameEntity> findByPlayer(Player player) {
		return findByPlayerPublicUuid(player.publicUuid());
	}
}
