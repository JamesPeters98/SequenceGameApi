package com.jamesdpeters.sequencegamerl.ml.sequence;

import java.util.List;
import java.util.Random;

public interface SequenceMovePolicy {

    SequenceMoveOption choose(List<SequenceMoveOption> legalMoves, Random random);
}
