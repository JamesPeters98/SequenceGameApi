package com.jamesdpeters.SequenceGame.game.persistence.entity;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.player.Player;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
		name = "game_player",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_game_player_public", columnNames = {"game_id", "public_uuid"}),
				@UniqueConstraint(name = "uk_game_player_private", columnNames = {"game_id", "private_uuid"}),
				@UniqueConstraint(name = "uk_game_player_turn_order", columnNames = {"game_id", "turn_order"})
		}
)
@Getter
@Setter
@NoArgsConstructor
public class GamePlayerEntity {

	public GamePlayerEntity(Player player) {
		this.publicUuid = player.publicUuid();
		this.privateUuid = player.privateUuid();
		this.name = player.name();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Column(name = "public_uuid", nullable = false, updatable = false)
	private UUID publicUuid;

	@Column(name = "private_uuid", nullable = false, updatable = false)
	private UUID privateUuid;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "turn_order", nullable = false)
	private int turnOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = "team", length = 16)
	private ChipColour team;

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GamePlayerHandCardEntity> handCards = new ArrayList<>();
}
