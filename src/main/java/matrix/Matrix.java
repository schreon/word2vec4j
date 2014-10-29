package matrix;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;

public class Matrix implements Serializable {
    public final int n;
    public final int m;
    public FloatBuffer buffer;
    private ByteBuffer byteBuffer;

    public Matrix(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public List<Similarity> mostSimilar(int index, int numResults) {
        PriorityBlockingQueue<Similarity> queue = new PriorityBlockingQueue<>();
        for (int i = 0; i < numResults; i++) {
            queue.put(new Similarity(-1.0f, 0));
        }
        ForkJoinPool.commonPool().invoke(new MostSimilar(queue, this, index, 0, n));
        List<Similarity> sims = new LinkedList<>();
        while (!queue.isEmpty()) {
            sims.add(queue.remove());
        }
        Collections.reverse(sims);
        return sims;
    }

    public float cosine(int i1, int i2) {
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
        pool.invoke(new FillNormal(this.buffer, 0, n * m, loc, std));
    }

    public void initMatrix() {
        byteBuffer = ByteBuffer.allocateDirect(n * m * 4).order(ByteOrder.nativeOrder());
        buffer = byteBuffer.asFloatBuffer();
    }

    public float get(final int i, final int j) {
        return buffer.get(i * m + j);
    }

    public void put(final int i, final int j, final float val) {
        buffer.put(i * m + j, val);
    }

    public void add(final int i, final int j, final float val) {
        final int idx = i * m + j;
        buffer.put(idx, buffer.get(idx) + val);
    }

    public void normalize(ForkJoinPool pool) {
        pool.invoke(new Normalize(this, 0, n));
    }

    public void normalize() {
        normalize(ForkJoinPool.commonPool());
    }


    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                s += String.format("%.2f ", get(i, j));
            }
            s += "\n";
        }
        return s;
    }

    public void saveBufferToFile(String fileName) {
        try {
            FileOutputStream fo = new FileOutputStream(fileName);
            FileChannel channel = fo.getChannel();
            channel.write(byteBuffer);
            channel.close();
            fo.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadBufferFromFile(String fileName) {
        try {
            FileInputStream fi = new FileInputStream(fileName);
            FileChannel channel = fi.getChannel();
            channel.read(byteBuffer);
            channel.close();
            fi.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}