package count;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RecursiveAction;


public abstract class FetchDocs<DocumentAction extends RecursiveAction> extends RecursiveAction {
    private static final String sentinel = new String();
    protected final Connection con;
    protected final int start;
    protected final int end;

    public FetchDocs(final Connection con, final int start, final int end) {
        this.con = con;
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

    public abstract DocumentAction createAction(String nextDoc);

    @Override
    protected void compute() { // compute directly
        try {
            final ArrayBlockingQueue<String> docQueue = new ArrayBlockingQueue<String>(512);
            startDocumentProducer(docQueue, con, start, end);
            String nextDoc = docQueue.take();
            int n = 0;
            Deque<DocumentAction> actions = new ArrayDeque<>(128);
            DocumentAction action;
            while (nextDoc != sentinel) {
                action = createAction(nextDoc);
                actions.add(action);
                n += 1;
                if (n % 64 == 0) {
                    invokeAll(actions);
                    actions.clear();
                }
                if (n % 10000 == 0) {
                    System.out.printf("Doc #%d %n", n);
                }
                nextDoc = docQueue.take();
            }
            if (!actions.isEmpty()) {
                invokeAll(actions);
            }
            actions.clear();
            System.out.println("Read sentinel, finished!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
