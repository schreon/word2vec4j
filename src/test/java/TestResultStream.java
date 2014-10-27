import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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

        final ConcurrentMap<String, Integer> wordCount = new ConcurrentHashMap<>(5000000);
        wiki.all().forEach(s -> {
            for (String word : s.split(" ")) {
                wordCount.putIfAbsent(word, 0);
                wordCount.put(word, wordCount.get(word) + 1);
            }
        });

        // remove all words which occur in less than 5 documents
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
