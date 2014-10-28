package matrix;

import java.nio.FloatBuffer;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class FillNormal extends RecursiveAction {
    public final static int LIMIT = 4096;

    private final int start;
    private final int end;
    private final FloatBuffer matrix;
    private final double loc;
    private final double std;

    public FillNormal(final FloatBuffer matrix, final int start, final int end, final double loc, final double std) {
        this.start = start;
        this.end = end;
        this.matrix = matrix;
        this.loc = loc;
        this.std = std;
    }

    public static void computeDirectly(final FloatBuffer matrix, final int start, final int end, final double loc, final double std) {
        for (int i = start; i < end; i++) {
            double r = loc + std * ThreadLocalRandom.current().nextGaussian();
            matrix.put(i, (float) r);
        }
    }

    @Override
    protected void compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            computeDirectly(matrix, start, end, loc, std);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * LIMIT) {
                split = start + LIMIT;
            } else {
                split = (start + end) / 2;
            }

            FillNormal left = new FillNormal(matrix, start, split, loc, std);
            FillNormal right = new FillNormal(matrix, split, end, loc, std);
            invokeAll(left, right);
        }
    }
}
