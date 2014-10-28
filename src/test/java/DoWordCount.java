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

public class DoWordCount {
    public static void main(String[] args) {
        try {

            final String wikiUrl = "jdbc:sqlite:" + (new File("/home/schreon/Downloads/wiki.db").toURI().toURL());


            final Map<String, Integer> wordCount = new ConcurrentHashMap<>(8000000);
            int offset = 0;
            int num = 2000000;
            System.out.println("Start");
            long start, end;
            double res_sec;
            Connection con = JDBC.createConnection(wikiUrl, new Properties());
            start = System.nanoTime();
            FetchDocs fetchDocs = new FetchDocs(wordCount, con, offset, offset + num);
            ForkJoinPool.commonPool().invoke(fetchDocs);
            end = System.nanoTime();
            res_sec = (double) num / ((end - start) / 1000000000.0);
            System.out.printf("%.2f%% @ #%d @ %.2f docs/sec - %.2f mio word types %n", (100.0 * offset) / num, offset, res_sec, wordCount.size() / 1000000.0);
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
