package com.jamesdpeters.SequenceGame.player;

import com.jamesdpeters.SequenceGame.deck.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public final class Player {
	private final UUID uuid;
	private final List<Card> hand = new ArrayList<>();


}
