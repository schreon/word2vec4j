import org.junit.Test;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TestResultStream {
    @Test
    public void testResultStream() throws Exception {
        WikiDAO wiki = new WikiDAO("/home/schreon/Downloads/wiki.db");

        final ConcurrentMap<String, Integer> wordCount = new ConcurrentHashMap<>(2000000);
        wiki.all().forEach(s -> {
            StringTokenizer tokenizer = new StringTokenizer(s);
            String word;
            while (tokenizer.hasMoreTokens()) {
                word = tokenizer.nextToken();
                if (word.length() < 60) {
                    wordCount.putIfAbsent(word, 0);
                    wordCount.put(word, wordCount.get(word) + 1);
                }
            }
        });

        // remove all words which occur less than 5 times
        Iterator<Map.Entry<String, Integer>> iter = wordCount.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Integer> entry = iter.next();
            if(entry.getValue() < 5) {
                iter.remove();
            }
        }

        System.out.println(wordCount.get("der"));

        FileOutputStream fo = new FileOutputStream("wordcount.bin");
        ObjectOutputStream os = new ObjectOutputStream(fo);
        os.writeObject(wordCount);

        System.out.println("Finished");
    }
}
