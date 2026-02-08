package com.jamesdpeters.sequencegamerl.web;

import com.jamesdpeters.sequencegamerl.ml.game.GameDescriptor;
import com.jamesdpeters.sequencegamerl.ml.game.GameRegistry;
import com.jamesdpeters.sequencegamerl.ml.training.TrainingRunRequest;
import com.jamesdpeters.sequencegamerl.ml.training.TrainingRunResult;
import com.jamesdpeters.sequencegamerl.ml.training.TrainingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TrainingController {

    private final GameRegistry gameRegistry;
    private final TrainingService trainingService;

    public TrainingController(GameRegistry gameRegistry, TrainingService trainingService) {
        this.gameRegistry = gameRegistry;
        this.trainingService = trainingService;
    }

    @GetMapping("/games")
    public List<GameDescriptor> listGames() {
        return gameRegistry.listDescriptors();
    }

    @PostMapping("/training/run")
    public TrainingRunResult runTraining(@RequestBody(required = false) TrainingRunRequest request) {
        return trainingService.runTraining(request);
    }

    @PostMapping("/training/examples/tictactoe")
    public TrainingRunResult runTicTacToeExample() {
        TrainingRunRequest request = new TrainingRunRequest();
        request.setGameId("tictactoe");
        return trainingService.runTraining(request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}
