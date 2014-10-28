import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

class FillNormal extends RecursiveAction {
    public final static int LIMIT = 128;

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
        if ((end - start) < LIMIT) {
            computeDirectly(matrix, start, end, loc, std);
        } else {
            int mid = (start + end) / 2;
            FillNormal left = new FillNormal(matrix, start, mid, loc, std);
            left.fork();
            FillNormal right = new FillNormal(matrix, mid, end, loc, std);
            right.fork();
            left.join();
            right.join();
        }
    }
}

public class Matrix implements Serializable {
    final int n;
    final int m;
    protected FloatBuffer matrix;

    public Matrix(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public void fillNormal(final double loc, final double std) {
        fillNormal(loc, std, ForkJoinPool.commonPool());
    }

    public void fillNormal(final double loc, final double std, ForkJoinPool pool) {
        pool.invoke(new FillNormal(this.matrix, 0, n * m, loc, std));
    }

    public void initMatrix() {
        matrix = ByteBuffer.allocateDirect(n * m * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public float get(int i, int j) {
        return matrix.get(i * m + j);
    }
}