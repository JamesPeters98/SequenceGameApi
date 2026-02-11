package com.jamesdpeters.SequenceGame.game.persistence.entity;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.game.Game;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game")
@Getter
@Setter
public class GameEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	@JdbcTypeCode(SqlTypes.UUID)
	private UUID id;

	@Column(name = "created_date", nullable = false)
	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	private Instant createdDate;

	@Column(name = "started_date")
	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	private Instant startedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private Game.Status status;

	@Column(name = "max_players", nullable = false)
	private int maxPlayers;

	@Enumerated(EnumType.STRING)
	@Column(name = "winner", length = 16)
	private ChipColour winner;

	@Column(name = "winning_sequence_length", nullable = false)
	private int winningSequenceLength;

	@Column(name = "dead_card_discarded_this_turn", nullable = false)
	private boolean deadCardDiscardedThisTurn;

	@Column(name = "current_player_public_uuid")
	@JdbcTypeCode(SqlTypes.UUID)
	private UUID currentPlayerPublicUuid;

	@Column(name = "host_player_public_uuid")
	@JdbcTypeCode(SqlTypes.UUID)
	private UUID hostPlayerPublicUuid;

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GamePlayerEntity> players = new ArrayList<>();

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GameBoardSpaceEntity> boardSpaces = new ArrayList<>();

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GameDeckCardEntity> deckCards = new ArrayList<>();

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GameMoveEntity> moveHistory = new ArrayList<>();

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GameTurnCountEntity> turnCounts = new ArrayList<>();
}
