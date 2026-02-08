package com.jamesdpeters.sequencegamerl.ml.game;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameRegistry {

    private final Map<String, LearningGame> gamesById;

    public GameRegistry(List<LearningGame> games) {
        Map<String, LearningGame> mappedGames = new LinkedHashMap<>();
        for (LearningGame game : games) {
            if (mappedGames.containsKey(game.id())) {
                throw new IllegalStateException("Duplicate game id registered: " + game.id());
            }
            mappedGames.put(game.id(), game);
        }
        this.gamesById = Map.copyOf(mappedGames);
    }

    public List<GameDescriptor> listDescriptors() {
        return gamesById.values().stream()
                .sorted(Comparator.comparing(LearningGame::id))
                .map(game -> new GameDescriptor(game.id(), game.displayName(), game.description()))
                .toList();
    }

    public LearningGame requireById(String gameId) {
        LearningGame game = gamesById.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Unknown game id: " + gameId);
        }
        return game;
    }
}
