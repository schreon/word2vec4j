package count;

import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;


public class CountWords extends RecursiveTask<Integer> {
    public final int LIMIT = 512;
    protected static BiFunction<String, Integer, Integer> addIfPresent = new BiFunction<String, Integer, Integer>() {
        @Override
        public Integer apply(String s, Integer integer) {
            return Math.min(Integer.MAX_VALUE-1, integer) + 1;
        }
    };
    final int start, end;
    final String[] tokens;
    final Map<String, Integer> wordMap;

    public CountWords(final Map<String, Integer> wordMap, final String[] tokens, final int start, final int end) {
        this.tokens = tokens;
        this.start = start;
        this.end = end;
        this.wordMap = wordMap;
    }

    public static Integer computeDirectly(final Map<String, Integer> wordMap, final String[] tokens, final int start, final int end) {
        String word;
        for (int i = start; i < end; i++) {
            word = tokens[i];
            try {
                wordMap.putIfAbsent(word, 0);
                wordMap.computeIfPresent(word, addIfPresent);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Exception for word " + word);
            }
        }
        return (end - start);
    }

    public int getLIMIT() {
        return LIMIT;
    }

    @Override
    protected Integer compute() {
        int diff = end - start;
        if (diff <= getLIMIT()) {
            return computeDirectly(wordMap, tokens, start, end);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * getLIMIT()) {
                split = start + getLIMIT();
            } else {
                split = (start + end) / 2;
            }
            CountWords left = new CountWords(wordMap, tokens, start, split);
            CountWords right = new CountWords(wordMap, tokens, split, end);
            invokeAll(left, right);
            try {
                return left.get() + right.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
