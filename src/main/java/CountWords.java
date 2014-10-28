import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

class CountWords extends RecursiveAction {
    final static String delimiter = " ".intern();

    int start, end;
    String doc;
    String[] tokens;
    Map<String, Integer> wordMap;

    public CountWords(Map<String, Integer> wordMap, String doc, int start, int end) {
        this.doc = doc;
        this.start = start;
        this.end = end;
        this.wordMap = wordMap;
    }

    public void computeDirectly(Map<String, Integer> wordMap, List<String> sList, int start, int end) {
        String[] tokens;
        List<CountWordsInString> counters = new LinkedList<>();
        for (int i = start; i < end; i++) {

        }
        // join counters
        for (CountWordsInString counter : counters) {
            counter.join();
        }
    }

    @Override
    protected void compute() {
        // split string
        tokens = doc.split(delimiter);
        // call counter
        CountWordsInString counter = new CountWordsInString(wordMap, tokens, 0, tokens.length);
        counter.fork();
        counter.join();
    }
}