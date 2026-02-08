package com.jamesdpeters.sequencegamerl.ml.tictactoe;

import com.jamesdpeters.sequencegamerl.ml.game.LearningGame;
import com.jamesdpeters.sequencegamerl.ml.game.TrainingSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeLearningGame implements LearningGame {

    private static final int BOARD_SIZE = 9;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = -1;
    private static final int CENTER = 4;

    private static final int[][] WIN_LINES = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };

    @Override
    public String id() {
        return "tictactoe";
    }

    @Override
    public String displayName() {
        return "TicTacToe";
    }

    @Override
    public String description() {
        return "Learns a policy that mimics a simple tactical TicTacToe player.";
    }

    @Override
    public int inputSize() {
        return BOARD_SIZE;
    }

    @Override
    public int outputSize() {
        return BOARD_SIZE;
    }

    @Override
    public TrainingSet sampleTrainingSet(int sampleCount, Random random) {
        INDArray features = Nd4j.zeros(sampleCount, BOARD_SIZE);
        INDArray labels = Nd4j.zeros(sampleCount, BOARD_SIZE);

        for (int row = 0; row < sampleCount; row++) {
            int[] board = generateBoard(random);
            int chosenMove = choosePolicyMove(board, random);

            for (int cell = 0; cell < BOARD_SIZE; cell++) {
                features.putScalar(row, cell, board[cell]);
            }
            labels.putScalar(row, chosenMove, 1.0);
        }

        return new TrainingSet(features, labels);
    }

    private int[] generateBoard(Random random) {
        while (true) {
            int movesPerPlayer = random.nextInt(5);
            int[] board = new int[BOARD_SIZE];
            List<Integer> shuffledCells = new ArrayList<>(BOARD_SIZE);
            for (int i = 0; i < BOARD_SIZE; i++) {
                shuffledCells.add(i);
            }
            Collections.shuffle(shuffledCells, random);

            for (int i = 0; i < movesPerPlayer; i++) {
                board[shuffledCells.get(i)] = PLAYER_X;
            }
            for (int i = movesPerPlayer; i < movesPerPlayer * 2; i++) {
                board[shuffledCells.get(i)] = PLAYER_O;
            }

            if (!availableMoves(board).isEmpty() && !isWinning(board, PLAYER_X) && !isWinning(board, PLAYER_O)) {
                return board;
            }
        }
    }

    private int choosePolicyMove(int[] board, Random random) {
        List<Integer> availableMoves = availableMoves(board);

        List<Integer> winningMoves = immediateWinningMoves(board, PLAYER_X, availableMoves);
        if (!winningMoves.isEmpty()) {
            return pickRandom(winningMoves, random);
        }

        List<Integer> blockingMoves = immediateWinningMoves(board, PLAYER_O, availableMoves);
        if (!blockingMoves.isEmpty()) {
            return pickRandom(blockingMoves, random);
        }

        if (board[CENTER] == 0) {
            return CENTER;
        }

        List<Integer> corners = collectIfAvailable(availableMoves, List.of(0, 2, 6, 8));
        if (!corners.isEmpty()) {
            return pickRandom(corners, random);
        }

        List<Integer> edges = collectIfAvailable(availableMoves, List.of(1, 3, 5, 7));
        if (!edges.isEmpty()) {
            return pickRandom(edges, random);
        }

        return availableMoves.get(0);
    }

    private List<Integer> immediateWinningMoves(int[] board, int player, List<Integer> availableMoves) {
        List<Integer> result = new ArrayList<>();
        for (int move : availableMoves) {
            board[move] = player;
            if (isWinning(board, player)) {
                result.add(move);
            }
            board[move] = 0;
        }
        return result;
    }

    private static List<Integer> collectIfAvailable(List<Integer> availableMoves, List<Integer> preferredCells) {
        List<Integer> picked = new ArrayList<>();
        for (int preferredCell : preferredCells) {
            if (availableMoves.contains(preferredCell)) {
                picked.add(preferredCell);
            }
        }
        return picked;
    }

    private static int pickRandom(List<Integer> moves, Random random) {
        return moves.get(random.nextInt(moves.size()));
    }

    private static List<Integer> availableMoves(int[] board) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) {
                moves.add(i);
            }
        }
        return moves;
    }

    private static boolean isWinning(int[] board, int player) {
        for (int[] line : WIN_LINES) {
            if (board[line[0]] == player && board[line[1]] == player && board[line[2]] == player) {
                return true;
            }
        }
        return false;
    }
}
