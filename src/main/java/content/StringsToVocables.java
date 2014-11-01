package content;

import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.util.concurrent.RecursiveAction;

/**
 * Created by schreon on 11/1/14.
 */
public class StringsToVocables extends RecursiveAction {

    public final static int LIMIT = 1000;
    private final String[] tokens;
    private final Vocable[] vocables;
    private final Vocabulary vocabulary;
    private final int start;
    private final int end;

    public StringsToVocables(final String[] tokens, final Vocable[] vocables, final Vocabulary vocabulary, int start, int end) {
        this.tokens = tokens;
        this.vocables = vocables;
        this.vocabulary = vocabulary;
        this.start = start;
        this.end = end;
    }
    @Override
    protected void compute() {
        if ((end-start) <= LIMIT) {
            for (int i=start; i < end; i++) {
                vocables[i] = vocabulary.get(tokens[i]);
            }
        } else {
            int split = (start + end) / 2;
            StringsToVocables left = new StringsToVocables(tokens, vocables, vocabulary, start, split);
            StringsToVocables right = new StringsToVocables(tokens, vocables, vocabulary, split, end);
            invokeAll(left, right);
            try {
                left.get();
                right.get();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
