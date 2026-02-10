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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
		name = "game_deck_card",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_deck_card_position", columnNames = {"game_id", "pile", "card_order"})
		}
)
@Getter
@Setter
public class GameDeckCardEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_id", nullable = false)
	private GameEntity game;

	@Enumerated(EnumType.STRING)
	@Column(name = "pile", nullable = false, length = 16)
	private DeckPile pile;

	@Column(name = "card_order", nullable = false)
	private int cardOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_suit", nullable = false, length = 16)
	private Card.Suit cardSuit;

	@Column(name = "card_value", nullable = false)
	private int cardValue;
}
