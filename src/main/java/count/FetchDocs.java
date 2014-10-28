package count;

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
    final Connection con;
    final Map<String, Integer> wordMap;
    final int start;
    final int end;

    public FetchDocs(final Map<String, Integer> wordMap, final Connection con, final int start, final int end) {
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
                    final PreparedStatement stmt = con.prepareStatement("SELECT content FROM pages LIMIT " + (end - start) + " OFFSET " + start);
                    stmt.setFetchSize(0);
                    final ResultSet resultSet = stmt.executeQuery();
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
            final ArrayBlockingQueue<String> docQueue = new ArrayBlockingQueue<String>(512);
            startDocumentProducer(docQueue, con, start, end);
            String nextDoc = docQueue.take();
            int n = 0;
            Deque<SplitDocument> splitters = new ArrayDeque<>(128);
            SplitDocument splitter;
            while (nextDoc != sentinel) {
                splitter = new SplitDocument(wordMap, nextDoc);
                splitters.add(splitter);
                n += 1;
                if (n % 64 == 0) {
                    invokeAll(splitters);
                    splitters.clear();
                }
                if (n % 10000 == 0) {
                    System.out.printf("Doc #%d %n", n);
                }
                nextDoc = docQueue.take();
            }
            if (!splitters.isEmpty()) {
                invokeAll(splitters);
            }
            splitters.clear();
            System.out.println("Read sentinel, finished!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
