package com.jamesdpeters.SequenceGame.game.persistence.entity;

import com.jamesdpeters.SequenceGame.board.ChipColour;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
		name = "game_board_space",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_board_space_coordinate", columnNames = {"game_id", "row_index", "column_index"})
		}
)
@Getter
@Setter
public class GameBoardSpaceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Column(name = "row_index", nullable = false)
	private int rowIndex;

	@Column(name = "column_index", nullable = false)
	private int columnIndex;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_suit", length = 16)
	private Card.Suit cardSuit;

	@Column(name = "card_value")
	private Integer cardValue;

	@Enumerated(EnumType.STRING)
	@Column(name = "chip_colour", length = 16)
	private ChipColour chipColour;

	@Column(name = "part_of_sequence", nullable = false)
	private boolean partOfSequence;
}
