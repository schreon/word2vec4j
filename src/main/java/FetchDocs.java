import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RecursiveAction;


public class FetchDocs extends RecursiveAction {
    private static final String sentinel = new String();
    Connection con;
    Map<String, Integer> wordMap;
    int start;
    int end;

    public FetchDocs(Map<String, Integer> wordMap, Connection con, int start, int end) {
        this.con = con;
        this.wordMap = wordMap;
        this.start = start;
        this.end = end;
    }

    public static void startDocumentProducer(final ArrayBlockingQueue<String> blockingQueue, final Connection con, final int start, final int end) throws Exception {
        new Thread() {
            @Override
            public void run() {
                try {
                    PreparedStatement stmt = con.prepareStatement("SELECT content FROM pages LIMIT " + (end - start) + " OFFSET " + start);
                    stmt.setFetchSize(0);
                    ResultSet resultSet = stmt.executeQuery();
                    int n = 0;
                    while (resultSet.next()) {
                        blockingQueue.put(resultSet.getString(1));
                        n += 1;
                    }
                    resultSet.getStatement().close();
                    System.out.printf("fetched %d rows %n", n);
                    blockingQueue.put(sentinel);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    @Override
    protected void compute() { // compute directly
        try {
            ArrayBlockingQueue<String> docQueue = new ArrayBlockingQueue<String>(100);
            startDocumentProducer(docQueue, con, start, end);
            String nextDoc = docQueue.take();
            int n = 0;
            Deque<CountWords> counters = new ArrayDeque<>(100);
            CountWords counter;
            while (nextDoc != sentinel) {
                counter = new CountWords(wordMap, nextDoc, start, end);
                counter.fork();
                counters.add(counter);
                n += 1;
                if (n % 100 == 0) {
                    while (!counters.isEmpty()) {
                        counters.pop().join();
                    }
                }
                if (n % 10000 == 0) {
                    System.out.printf("Doc #%d %n", n);
                }
                nextDoc = docQueue.take();
            }
            while (!counters.isEmpty()) {
                counters.pop().join();
            }
            System.out.println("Read sentinel, finished!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
