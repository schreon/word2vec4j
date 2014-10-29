package content;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by schreon on 10/28/14.
 */
public abstract class SplitDocument extends RecursiveTask<Integer> {

    private final String docString;

    public SplitDocument(final String docString) {
        this.docString = docString;
    }

    public abstract RecursiveTask<Integer> createTask(String[] tokens);

    @Override
    protected Integer compute() {
        String[] tokens = docString.split(" ");
        RecursiveTask<Integer> task = createTask(tokens);
        ForkJoinTask.invokeAll(task);
        try {
            return task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
