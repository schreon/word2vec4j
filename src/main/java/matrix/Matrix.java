package matrix;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ForkJoinPool;

public class Matrix implements Serializable {
    public final int n;
    public final int m;
    protected FloatBuffer matrix;

    public Matrix(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public float cosine(int i1, int j1, int i2, int j2) {
        float a, b;
        float c = 0.0f;
        for (int j = 0; j < m; j++) {
            a = get(i1, j);
            b = get(i2, j);
            c += a * b;
        }
        return c;
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

    public float get(final int i, final int j) {
        return matrix.get(i * m + j);
    }

    public void put(final int i, final int j, final float val) {
        matrix.put(i * m + j, val);
    }

    public void normalize(ForkJoinPool pool) {
        pool.invoke(new Normalize(this, 0, n));
    }

    public void normalize() {
        normalize(ForkJoinPool.commonPool());
    }
}