import matrix.Matrix;
import org.junit.Test;

import java.nio.FloatBuffer;

/**
 * Created by schreon on 10/29/14.
 */
public class TestBuffer {

    public static void viewAdd(FloatBuffer buffer, float scalar, int offset, int length, float[] view) {
        buffer.position(offset);
        buffer.get(view, offset, length);
        for (int i=0; i< length; i++) {
            view[i] += scalar;
        }
        buffer.put(view, offset, length);
    }


    public static void directAdd(FloatBuffer buffer, float scalar, int offset, int length) {
        for (int i=offset; i<offset+length; i++) {
            buffer.put(i, buffer.get(i)+scalar);
        }
    }

    @Test
    public void testBuffer() throws Exception {
        Matrix mat = new Matrix(100,128);
        mat.initMatrix();

        System.out.println(mat.buffer.isDirect());
        long start, end;
        double dur;
        int n = 1000000;
        float[] view = new float[128];



        start = System.nanoTime();
        for (int it=0; it < n; it++) {
            directAdd(mat.buffer, 0.001f, 0, 128);
        }
        end = System.nanoTime();
        dur = (double)(end - start) / n;
        System.out.printf("directAdd: %.4f ns %n", dur);



        start = System.nanoTime();
        for (int it=0; it < n; it++) {
            viewAdd(mat.buffer, 0.001f, 0, 128, view);
        }
        end = System.nanoTime();
        dur = (double)(end - start) / n;
        System.out.printf("viewAdd: %.4f ns %n", dur);
    }
}
