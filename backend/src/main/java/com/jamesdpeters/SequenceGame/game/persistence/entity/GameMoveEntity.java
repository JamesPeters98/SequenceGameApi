package com.jamesdpeters.SequenceGame.game.persistence.entity;

import com.jamesdpeters.SequenceGame.card.Card;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
		name = "game_move",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_game_move_order", columnNames = {"game_id", "move_order"})
		}
)
@Getter
@Setter
public class GameMoveEntity {

	@Id
	@SequenceGenerator(name = "game_move_seq_gen", sequenceName = "game_move_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_move_seq_gen")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Column(name = "move_order", nullable = false)
	private int moveOrder;

	@Column(name = "player_public_uuid", nullable = false)
	private UUID playerPublicUuid;

	@Column(name = "row_index", nullable = false)
	private int rowIndex;

	@Column(name = "column_index", nullable = false)
	private int columnIndex;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_suit", nullable = false, length = 16)
	private Card.Suit cardSuit;

	@Column(name = "card_value", nullable = false)
	private int cardValue;
}
