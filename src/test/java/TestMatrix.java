import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TestMatrix {
    @Test
    public void testMatrix() throws Exception {
        System.out.println("Allocating huge amount of memory ...");
        List<Matrix> matrices = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            Matrix mat = new Matrix(2500000, 128);
            System.out.println("Allocating ...");
            mat.initMatrix();
            System.out.printf("Matrix %d allocated. %n", i);
            mat.fillNormal(0.0, 0.01);
            System.out.println("Randomized");
            matrices.add(mat);

            System.out.printf("[ ");
            for (int j = 0; j < 128; j++) {
                System.out.printf("%.5f ", mat.get(2500000 - 1, j));
            }
            System.out.printf("]%n");
        }
    }
}
