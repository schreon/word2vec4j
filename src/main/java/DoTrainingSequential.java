import common.ObjectPool;
import content.DocumentsProvider;
import content.StringsToVocables;
import learn.TrainDocument;
import learn.TrainWorkbench;
import matrix.Matrix;
import org.sqlite.JDBC;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by schreon on 10/31/14.
 */
public class DoTrainingSequential {

    public static void main(String[] args) {

        try {

            int vecSize = 100;
            final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");
            final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);
            final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), vecSize);

            syn0.initDirectBuffer();
            syn1.initDirectBuffer();

            syn0.fillNormal(0.0, 0.01);
            syn1.fillNormal(0.0, 0.01);


            final int finalVecSize = vecSize;
            ObjectPool<TrainWorkbench> workBenchPool = new ObjectPool<TrainWorkbench>(100) {
                @Override
                protected TrainWorkbench createObject() {
                    return new TrainWorkbench(finalVecSize, syn0, syn1);
                }
            };
            TrainDocument.LIMIT = 16;


            final int offset = 0;
            final int num = 50000;

            DocumentsProvider provider = new DocumentsProvider() {
                long startTime, duration, num_words, num_docs;
                double kwords_sec, docs_sec, progress;

                @Override
                public void onStart() {
                    num_docs = 0;
                    num_words = 0;
                    startTime = System.nanoTime();
                }

                @Override
                public void onDocument(String nextDoc) {
                    String[] tokens = nextDoc.split(" ");
                    Vocable[] words = new Vocable[tokens.length];
                    ForkJoinPool.commonPool().invoke(new StringsToVocables(tokens, words, vocabulary, 0, tokens.length));
                    TrainDocument task = new TrainDocument(workBenchPool, words, 0, words.length, 0.001f);
                    ForkJoinPool.commonPool().invoke(task);
                    try {
                        num_words += task.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                    num_docs += 1;
                    if (num_docs % 1000 == 0) {
                        duration = System.nanoTime() - startTime;
                        kwords_sec = num_words * 1000000.0 / duration;
                        docs_sec = num_docs * 1000000000.0 / duration;
                        progress = (100.0 * num_docs) / (num - offset);
                        System.out.printf("%.2f%%, %.2f docs/sec, %.2f k words/sec %n", progress, docs_sec, kwords_sec);
                    }
                }

                @Override
                public void onEnd() {
                    duration = System.nanoTime() - startTime;
                    kwords_sec = num_words * 1000000000.0 / duration;
                    docs_sec = num_docs * 1000000000.0 / duration;
                    progress = (100.0 * num_docs) / (num - offset);
                    System.out.println("--- finished ---");
                    System.out.printf("%.2f%%, %.2f docs/sec, %.2f k words/sec %n", progress, docs_sec, kwords_sec);
                }
            };


            final String wikiUrl = "jdbc:sqlite:" + (new File("/home/schreon/Downloads/wiki.db").toURI().toURL());
            Connection con = JDBC.createConnection(wikiUrl, new Properties());

            provider.start(con, offset, num);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
