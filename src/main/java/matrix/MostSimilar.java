package matrix;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveAction;

/**
 * Created by schreon on 10/29/14.
 */
public class MostSimilar extends RecursiveAction {
    public static final int LIMIT = 512;
    final PriorityBlockingQueue<Similarity> queue;
    final Matrix matrix;
    final int index;
    final int start;
    final int end;

    public MostSimilar(final PriorityBlockingQueue<Similarity> queue, final Matrix matrix, final int index, final int start, final int end) {
        this.queue = queue;
        this.matrix = matrix;
        this.index = index;
        this.start = start;
        this.end = end;
    }

    // queue must be initialized with as many 0.0s as there should be results!
    public static void computeDirectly(final PriorityBlockingQueue<Similarity> queue, final Matrix matrix, final int index, final int start, final int end) {
        float sim;
        for (int other = start; other < end; other++) {
            sim = matrix.cosine(index, other);
            queue.add(new Similarity(sim, other));
            queue.remove();
        }
    }

    @Override
    protected void compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            computeDirectly(queue, matrix, index, start, end);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * LIMIT) {
                split = start + LIMIT;
            } else {
                split = (start + end) / 2;
            }

            MostSimilar left = new MostSimilar(queue, matrix, index, start, split);
            MostSimilar right = new MostSimilar(queue, matrix, index, split, end);
            invokeAll(left, right);
        }
    }
}
