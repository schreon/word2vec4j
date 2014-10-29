import matrix.Matrix;
import org.junit.Test;

public class TestMatrix {
    @Test
    public void testMatrix() throws Exception {
        Matrix mat = new Matrix(10, 10);
        mat.initMatrix();
        mat.fillNormal(0.0, 0.01);
        mat.normalize();

        System.out.println(mat);
        for (int i = 0; i < 100; i++) {
            assert 6 == mat.mostSimilar(6, 1).get(0).index;
        }

        mat.saveBufferToFile("matrixtest.bin");

        Matrix mat2 = new Matrix(10, 10);
        mat2.initMatrix();
        mat2.loadBufferFromFile("matrixtest.bin");

        for (int i = 0; i < 10; i++) {
            for (int j=0; j < 10; j++) {
                assert mat.get(i,j) == mat.get(i,j);
            }
        }

    }
}
