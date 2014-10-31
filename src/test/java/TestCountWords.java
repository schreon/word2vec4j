import count.CountWords;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by schreon on 10/30/14.
 */
public class TestCountWords {
    @Test
    public void testCountWords() {
        final Map<String, Integer> wordCount = new ConcurrentHashMap<>();

        String[] s = "dies ist ein schöner test text den ich mir gerade so zum spaß ausgedacht habe test text".split(" ");

        class MyWordCount extends CountWords {
            public int getLIMIT() {
                return 3;
            }

            public MyWordCount(Map<String, Integer> wordMap, String[] tokens, int start, int end) {
                super(wordMap, tokens, start, end);
            }
        }
        ForkJoinPool.commonPool().invoke(new CountWords(wordCount, s, 0, s.length));

        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            System.out.printf("%s : %d %n", entry.getKey(), entry.getValue());
        }

        assert wordCount.get("test") == 2;
        assert wordCount.get("text") == 2;
    }
}
