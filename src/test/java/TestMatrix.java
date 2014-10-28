import matrix.Matrix;
import org.junit.Test;

public class TestMatrix {
    @Test
    public void testMatrix() throws Exception {
        Matrix mat = new Matrix(100, 128);
        mat.initMatrix();
        mat.fillNormal(0.0, 0.01);
        mat.normalize();
        for (int i = 0; i < 100; i++) {
            assert 6 == mat.mostSimilar(6, 1).get(0).index;
        }
    }
}
