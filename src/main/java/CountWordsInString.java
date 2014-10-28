import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.function.BiFunction;
import java.util.function.Function;


public class CountWordsInString extends RecursiveAction {
    final static int LIMIT = 100;
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
    int start, end;
    String[] tokens;
    Map<String, Integer> wordMap;
    int mid;

    public CountWordsInString(Map<String, Integer> wordMap, String[] tokens, int start, int end) {
        this.tokens = tokens;
        this.start = start;
        this.end = end;
        this.wordMap = wordMap;
    }

    public static void computeDirectly(Map<String, Integer> wordMap, String[] tokens, int start, int end) {
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
    }

    @Override
    protected void compute() {
        if ((end - start) < LIMIT) {
            computeDirectly(wordMap, tokens, start, end);
        } else {
            mid = (start + end) / 2;
            CountWordsInString left = new CountWordsInString(wordMap, tokens, start, mid);
            left.fork();
            CountWordsInString right = new CountWordsInString(wordMap, tokens, mid, end);
            right.fork();
            left.join();
            right.join();
        }
    }
}
