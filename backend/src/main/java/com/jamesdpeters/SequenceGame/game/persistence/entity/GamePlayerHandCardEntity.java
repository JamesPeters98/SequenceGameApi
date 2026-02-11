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
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

@Entity
@Table(
		name = "game_player_hand_card",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_hand_card_position", columnNames = {"player_id", "card_order"})
		}
)
@Getter
@Setter
public class GamePlayerHandCardEntity {

	@Id
	@SequenceGenerator(name = "game_player_hand_card_seq_gen", sequenceName = "game_player_hand_card_seq", allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_player_hand_card_seq_gen")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private GamePlayerEntity player;

	@Column(name = "card_order", nullable = false)
	private int cardOrder;

	@JdbcType(PostgreSQLEnumJdbcType.class)
	@Enumerated(EnumType.STRING)
	@Column(name = "card_suit", nullable = false, length = 16, columnDefinition = "card_suit")
	private Card.Suit cardSuit;

	@Column(name = "card_value", nullable = false)
	private int cardValue;
}
