package content;

import count.CountWords;

import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * Created by schreon on 10/28/14.
 */
public class SplitDocument extends RecursiveAction {

    private final Map<String, Integer> wordMap;
    private final String docString;

    public SplitDocument(final Map<String, Integer> wordMap, final String docString) {
        this.wordMap = wordMap;
        this.docString = docString;
    }

    @Override
    protected void compute() {
        String[] tokens = docString.split(" ");
        ForkJoinTask.invokeAll(new CountWords(wordMap, tokens, 0, tokens.length));
    }
}
