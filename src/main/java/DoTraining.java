import common.ObjectPool;
import content.FetchDocs;
import content.SplitDocument;
import learn.TrainDocument;
import learn.TrainWorkbench;
import matrix.Matrix;
import matrix.Similarity;
import org.sqlite.JDBC;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DoTraining {

    public static void printMostSimilar(String queryWord, int numResults, Vocabulary vocabulary, Matrix syn0) {
        Vocable vocable = vocabulary.get(queryWord);
        for (Similarity sim : syn0.mostSimilar(vocable.getIndex(), numResults)){
            String hit = vocabulary.getWordByIndex(sim.index);
            System.out.printf("%s : %.2f %n", hit, sim.similarity);
        }
    }

    public static void main(String[] args) {
        try {
            int vecSize = 100;
            final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.50k.bin");
            final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);
            final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), vecSize);

            syn0.initDirectBuffer();
            syn1.initDirectBuffer();

            syn0.fillNormal(0.0, 0.01);
            syn1.fillNormal(0.0, 0.01);


            final String wikiUrl = "jdbc:sqlite:" + (new File("/home/schreon/Downloads/wiki.db").toURI().toURL());

            int offset = 0;
            int num = 50000;
            System.out.println("Start");
            Connection con = JDBC.createConnection(wikiUrl, new Properties());

            final int finalVecSize = vecSize;
            ObjectPool<TrainWorkbench> workBenchPool = new ObjectPool<TrainWorkbench>(100) {
                @Override
                protected TrainWorkbench createObject() {
                    return new TrainWorkbench(finalVecSize, syn0, syn1);
                }
            };
            TrainDocument.LIMIT = 128;

            int alpha;
            class SplitThenTrain extends SplitDocument {
                public SplitThenTrain(String docString) {
                    super(docString);
                }
                @Override
                public RecursiveTask<Integer> createTask(String[] tokens) {
                    Vocable[] words = new Vocable[tokens.length];
                    for (int i=0; i < tokens.length; i++) {
                        words[i] = vocabulary.get(tokens[i]);
                    }
                    return new TrainDocument(workBenchPool, words, 0, tokens.length, 0.001f); // TODO: dynamic learn rate
                }
            }

            FetchDocs fetchDocs = new FetchDocs(con, offset, offset + num, 500) {
                @Override
                public RecursiveTask<Integer> createTask(String nextDoc) {
                    return new SplitThenTrain(nextDoc);
                }

                @Override
                protected void onInterval(int iteration) {
                    // TODO: lower alpha
                }
            };


            ForkJoinPool pool = new ForkJoinPool();

            System.out.println("Initialized, start training");
            //ForkJoinPool.commonPool().invoke(fetchDocs);
            pool.invoke(fetchDocs);

            con.close();

            System.out.println("Finished Training.");

            syn0.saveBufferToFile("syn0test.bin");
            syn1.saveBufferToFile("syn1test.bin");

            syn0.normalize();

            printMostSimilar("holz", 10, vocabulary, syn0);
            System.out.println("-------------");
            printMostSimilar("haus", 10, vocabulary, syn0);
            System.out.println("-------------");
            printMostSimilar("chemie", 10, vocabulary, syn0);
            System.out.println("-------------");
            printMostSimilar("könig", 10, vocabulary, syn0);
            System.out.println("-------------");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
