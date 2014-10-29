package content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;


public abstract class FetchDocs extends RecursiveAction {
    private static final String sentinel = new String();
    protected final Connection con;
    protected final int start;
    protected final int end;
    protected final int logInterval;

    public FetchDocs(final Connection con, final int start, final int end, final int logInterval) {
        this.con = con;
        this.start = start;
        this.end = end;
        this.logInterval = logInterval;
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

    public abstract RecursiveTask<Integer> createTask(String nextDoc);

    protected void onInterval(int iteration) {}

    @Override
    protected void compute() { // compute directly
        try {
            final ArrayBlockingQueue<String> docQueue = new ArrayBlockingQueue<String>(512);
            startDocumentProducer(docQueue, con, start, end);
            String nextDoc = docQueue.take();
            int n = 0;
            long w = 0;
            Deque<RecursiveTask<Integer>> tasks = new ArrayDeque<>(128);
            RecursiveTask<Integer> task;
            long start, end;
            double res_sec, words_sec;
            start = System.nanoTime();
            while (nextDoc != sentinel) {
                task = createTask(nextDoc);
                tasks.add(task);
                n += 1;
                if (n % 64 == 0) {
                    invokeAll(tasks);
                    for (RecursiveTask<Integer> t : tasks) {
                        w += t.get();
                    }
                    tasks.clear();
                }
                if (n % logInterval == 0) {
                    end = System.nanoTime();
                    res_sec = (double) n / ((end - start) / 1000000000.0);
                    words_sec = (w / 1000000.0) / ((end - start) / (1000000000.0));
                    System.out.printf("Doc #%d @ %.2f docs/sec, %.3f mio words/sec %n", n, res_sec, words_sec);
                    onInterval(n);
                }
                nextDoc = docQueue.take();
            }
            if (!tasks.isEmpty()) {
                invokeAll(tasks);
            }
            tasks.clear();
            end = System.nanoTime();
            res_sec = (double) n / ((end - start) / 1000000000.0);
            words_sec = (w / 1000000.0) / ((end - start) / (1000000000.0));
            System.out.printf("Doc #%d @ %.2f docs/sec, %.2f mio words/sec %n", n, res_sec, words_sec);
            System.out.println("Read sentinel, finished!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
