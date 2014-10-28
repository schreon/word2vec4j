package matrix;

import java.util.concurrent.RecursiveAction;

/**
 * Created by schreon on 10/28/14.
 */
class Normalize extends RecursiveAction {
    public final int LIMIT = 512;
    private final Matrix matrix;
    private final int start;
    private final int end;

    public Normalize(final Matrix matrix, final int start, final int end) {
        this.matrix = matrix;
        this.start = start;
        this.end = end;
    }

    public static void computeDirectly(final Matrix matrix, final int start, final int end) {
        float norm, value;
        for (int i = start; i < end; i++) {
            norm = 0.0f;
            for (int j = 0; j < matrix.m; j++) {
                value = matrix.get(i, j);
                norm += value * value;
            }
            norm = (float) Math.sqrt(norm);
            for (int j = 0; j < matrix.m; j++) {
                value = matrix.get(i, j);
                matrix.put(i, j, value / norm);
            }
        }
    }

    @Override
    protected void compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            computeDirectly(matrix, start, end);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * LIMIT) {
                split = start + LIMIT;
            } else {
                split = (start + end) / 2;
            }

            Normalize left = new Normalize(matrix, start, split);
            Normalize right = new Normalize(matrix, split, end);
            invokeAll(left, right);
        }
    }
}
