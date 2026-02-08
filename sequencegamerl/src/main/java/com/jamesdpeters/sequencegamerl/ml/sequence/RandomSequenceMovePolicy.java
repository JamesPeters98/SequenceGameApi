package com.jamesdpeters.sequencegamerl.ml.sequence;

import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class RandomSequenceMovePolicy implements SequenceMovePolicy {

    @Override
    public SequenceMoveOption choose(List<SequenceMoveOption> legalMoves, Random random) {
        return legalMoves.get(random.nextInt(legalMoves.size()));
    }
}
