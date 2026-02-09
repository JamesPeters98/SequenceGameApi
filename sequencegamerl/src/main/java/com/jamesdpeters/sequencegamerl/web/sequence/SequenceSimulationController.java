package com.jamesdpeters.sequencegamerl.web.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRandomPlayService;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRandomRunRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRandomRunResult;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRlTrainingRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRlTrainingResult;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRlTrainingService;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRlEvaluationRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceRlEvaluationResult;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceSessionInitRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceSessionInitResult;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceSessionStartRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceSessionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sequence")
public class SequenceSimulationController {

    private final SequenceRandomPlayService randomPlayService;
    private final SequenceRlTrainingService rlTrainingService;

    public SequenceSimulationController(
            SequenceRandomPlayService randomPlayService,
            SequenceRlTrainingService rlTrainingService) {
        this.randomPlayService = randomPlayService;
        this.rlTrainingService = rlTrainingService;
    }

    @PostMapping("/random-run")
    public SequenceRandomRunResult randomRun(@RequestBody(required = false) SequenceRandomRunRequest request) {
        return randomPlayService.runRandomGame(request);
    }

    @PostMapping("/train")
    public SequenceRlTrainingResult train(@RequestBody(required = false) SequenceRlTrainingRequest request) {
        SequenceRlTrainingRequest effectiveRequest = request == null ? new SequenceRlTrainingRequest() : request;
        return rlTrainingService.train(effectiveRequest);
    }

    @PostMapping("/evaluate")
    public SequenceRlEvaluationResult evaluate(@RequestBody(required = false) SequenceRlEvaluationRequest request) {
        SequenceRlEvaluationRequest effectiveRequest = request == null ? new SequenceRlEvaluationRequest() : request;
        return rlTrainingService.evaluate(effectiveRequest);
    }

    @PostMapping("/session/init")
    public SequenceSessionInitResult initSession(@RequestBody(required = false) SequenceSessionInitRequest request) {
        return randomPlayService.initSession(request);
    }

    @PostMapping("/session/{gameUuid}/start")
    public SequenceSessionStatus startSession(
            @PathVariable String gameUuid,
            @RequestBody(required = false) SequenceSessionStartRequest request) {
        return randomPlayService.startSession(gameUuid, request);
    }

    @GetMapping("/session/{gameUuid}")
    public SequenceSessionStatus sessionStatus(@PathVariable String gameUuid) {
        return randomPlayService.getSessionStatus(gameUuid);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}
