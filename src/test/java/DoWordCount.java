import content.FetchDocs;
import content.SplitDocument;
import count.CountWords;
import org.sqlite.JDBC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DoWordCount {
    public static void main(String[] args) {
        try {

            final String wikiUrl = "jdbc:sqlite:" + (new File("/home/schreon/Downloads/wiki.db").toURI().toURL());


            final Map<String, Integer> wordCount = new ConcurrentHashMap<>(8000000);
            int offset = 0;
            int maxDocs = 2000000;
            System.out.println("Start");

            Connection con = JDBC.createConnection(wikiUrl, new Properties());

            class SplitThenCount extends SplitDocument {
                public SplitThenCount(String docString) {
                    super(docString);
                }
                @Override
                public RecursiveTask<Integer> createTask(String[] tokens) {
                    return new CountWords(wordCount, tokens, 0, tokens.length);
                }
            }

            FetchDocs fetchDocs = new FetchDocs(con, offset, offset + maxDocs, 10000) {
                @Override
                public RecursiveTask<Integer> createTask(String nextDoc) {
                    return new SplitThenCount(nextDoc);
                }
            };
            ForkJoinPool pool = new ForkJoinPool();

            //ForkJoinPool.commonPool().invoke(fetchDocs);
            pool.invoke(fetchDocs);
            con.close();

            System.out.println("Finish");
            System.out.println("Remove words which occur less than 5 times.");
            // remove all words which occur less than 5 times
            Iterator<Map.Entry<String, Integer>> iter = wordCount.entrySet().iterator();
            Map.Entry<String, Integer> entry;
            while (iter.hasNext()) {
                entry = iter.next();
                if (entry.getValue() < 5) {
                    iter.remove();
                }
            }
            System.out.printf("%.2f mio word types remaining. %n", wordCount.size() / 1000000.0);
            FileOutputStream fo = new FileOutputStream("wordcount.bin");
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(wordCount);

            os.close();
            fo.close();

            System.out.println("Finished");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
