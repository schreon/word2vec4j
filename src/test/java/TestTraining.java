import content.FetchDocs;
import content.SplitDocument;
import learn.TrainSentence;
import matrix.Matrix;
import matrix.Similarity;
import org.junit.Test;
import org.sqlite.JDBC;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestTraining {

    public static void printMostSimilar(String queryWord, int numResults, Vocabulary vocabulary, Matrix syn0) {
        Vocable vocable = vocabulary.get(queryWord);
        for (Similarity sim : syn0.mostSimilar(vocable.getIndex(), numResults)){
            String hit = vocabulary.getWordByIndex(sim.index);
            System.out.printf("%s : %.2f %n", hit, sim.similarity);
        }
    }

    @Test
    public void testTraining() throws Exception {
        int vecSize = 128;
        final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");
        final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);
        final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), vecSize);

        syn0.initMatrix();
        syn1.initMatrix();

        syn0.fillNormal(0.0, 0.01);
        syn1.fillNormal(0.0, 0.01);


        final String wikiUrl = "jdbc:sqlite:" + (new File("/home/schreon/Downloads/wiki.db").toURI().toURL());

        int offset = 0;
        int num = 2000000;
        System.out.println("Start");
        Connection con = JDBC.createConnection(wikiUrl, new Properties());

        final String empty = "".intern();

        class SplitThenTrain extends SplitDocument {
            public SplitThenTrain(String docString) {
                super(docString);
            }
            @Override
            public RecursiveTask<Integer> createTask(String[] tokens) {
                for (int i=0; i < tokens.length; i++) {
                    if (!vocabulary.containsKey(tokens[i].intern())) {
                        tokens[i] = empty;
                    }
                }
                return new TrainSentence(syn0, syn1, tokens, vocabulary, 0, tokens.length, 0.025f); // TODO: dynamic learn rate
            }
        }

        FetchDocs fetchDocs = new FetchDocs(con, offset, offset + num, 500) {
            @Override
            public RecursiveTask<Integer> createTask(String nextDoc) {
                return new SplitThenTrain(nextDoc);
            }
        };


        ForkJoinPool pool = new ForkJoinPool();

        System.out.println("Initialized, start training");
        //ForkJoinPool.commonPool().invoke(fetchDocs);
        pool.invoke(fetchDocs);

        con.close();

        System.out.println("Finished Training.");

        syn0.normalize();

        printMostSimilar("haus", 10, vocabulary, syn0);
        System.out.println("-------------");
        printMostSimilar("baum", 10, vocabulary, syn0);
        System.out.println("-------------");
        printMostSimilar("schmerz", 10, vocabulary, syn0);
        System.out.println("-------------");
        printMostSimilar("chemisch", 10, vocabulary, syn0);
        System.out.println("-------------");


    }
}
