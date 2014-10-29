import matrix.Matrix;
import matrix.Similarity;
import org.junit.Test;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

/**
 * Created by schreon on 10/29/14.
 */
public class TestModel {
    public static void printMostSimilar(String queryWord, int numResults, Vocabulary vocabulary, Matrix syn0) {
        Vocable vocable = vocabulary.get(queryWord);
        for (Similarity sim : syn0.mostSimilar(vocable.getIndex(), numResults)){
            String hit = vocabulary.getWordByIndex(sim.index);
            System.out.printf("%s : %.2f %n", hit, sim.similarity);
        }
    }

    @Test
    public void testModel() {
        int vecSize = 128;
        final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");
        final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);

        syn0.initMatrix();
        syn0.loadBufferFromFile("syn0.bin");
        syn0.normalize();

        printMostSimilar("holz", 10, vocabulary, syn0);
        System.out.println("----");

        printMostSimilar("metall", 10, vocabulary, syn0);
        System.out.println("----");

        printMostSimilar("schraubenzieher", 10, vocabulary, syn0);
        System.out.println("----");
    }
}
