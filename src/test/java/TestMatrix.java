import matrix.Matrix;
import matrix.Similarity;
import org.junit.Test;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

public class TestMatrix {

    public static void printMostSimilar(String queryWord, int numResults, Vocabulary vocabulary, Matrix syn0) {
        Vocable vocable = vocabulary.get(queryWord);
        for (Similarity sim : syn0.mostSimilar(vocable.getIndex(), numResults)){
            String hit = vocabulary.getWordByIndex(sim.index);
            System.out.printf("%s : %.2f %n", hit, sim.similarity);
        }
    }

    @Test
    public void testMatrix() throws Exception {
        Matrix mat = new Matrix(10, 10);
        mat.initDirectBuffer();
        mat.fillNormal(0.0, 0.01);
        mat.normalize();

        System.out.println(mat);
        for (int i = 0; i < 100; i++) {
            assert 6 == mat.mostSimilar(6, 1).get(0).index;
        }

        mat.saveBufferToFile("matrixtest.bin");

        Matrix mat2 = new Matrix(10, 10);
        mat2.initDirectBuffer();
        mat2.loadBufferFromFile("matrixtest.bin");

        for (int i = 0; i < 10; i++) {
            for (int j=0; j < 10; j++) {
                assert mat.get(i,j) == mat.get(i,j);
            }
        }
    }

    @Test
    public void testModel() throws Exception {
        int vecSize = 200;
        final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");

        double lengths = 0.0;
        double number = 0.0;
        for (Vocable voc : vocabulary.values()) {
            lengths += voc.getPath().length;
            number += 1.0;
        }
        System.out.printf("Average path length: %.2f %n", (lengths / number));


        final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);
        final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), vecSize);

        syn0.initDirectBuffer();
        syn1.initDirectBuffer();

        syn0.loadBufferFromFile("syn0test.bin");
        syn1.loadBufferFromFile("syn1test.bin");

        syn0.normalize();

        printMostSimilar("schlafen", 10, vocabulary, syn0);
        System.out.println("-------------");
    }
}
