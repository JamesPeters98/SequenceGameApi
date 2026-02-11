package com.jamesdpeters.SequenceGame.game.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
		name = "game_turn_count",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_game_turn_player", columnNames = {"game_id", "player_public_uuid"})
		}
)
@Getter
@Setter
public class GameTurnCountEntity {

	@Id
	@SequenceGenerator(name = "game_turn_count_seq_gen", sequenceName = "game_turn_count_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_turn_count_seq_gen")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Column(name = "player_public_uuid", nullable = false)
	private UUID playerPublicUuid;

	@Column(name = "turn_count", nullable = false)
	private int turnCount;
}
