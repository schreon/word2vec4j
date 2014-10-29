package count;

import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.Function;


public class CountWords extends RecursiveTask<Integer> {
    final static int LIMIT = 512;
    protected static BiFunction<String, Integer, Integer> addIfPresent = new BiFunction<String, Integer, Integer>() {
        @Override
        public Integer apply(String s, Integer integer) {
            return integer + 1;
        }
    };
    protected static Function<String, Integer> setIfAbsent = new Function<String, Integer>() {
        @Override
        public Integer apply(String s) {
            return 0;
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
                wordMap.computeIfAbsent(word, setIfAbsent);
                wordMap.computeIfPresent(word, addIfPresent);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Exception for word " + word);
            }
        }
        return (end - start);
    }

    @Override
    protected Integer compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            return computeDirectly(wordMap, tokens, start, end);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * LIMIT) {
                split = start + LIMIT;
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
