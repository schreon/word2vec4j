import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Created by leon on 25.10.14.
 */
public class TestResultStream {
    @Test
    public void testResultStream() throws Exception {
        WikiDAO wiki = new WikiDAO("/home/leon/Downloads/wiki.db");

        final ConcurrentMap<String, Integer> wordCount = new ConcurrentHashMap<>();
        wiki.all().limit(200).forEach(s -> {
            for (String word : new HashSet<String>(Arrays.asList(s.split(" ")))) {
                wordCount.putIfAbsent(word, 0);
                wordCount.put(word, wordCount.get(word)+1);
            };
        });

        System.out.println(wordCount.get("wegen"));
    }
}
