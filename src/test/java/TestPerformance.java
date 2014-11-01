import common.ObjectPool;
import learn.TrainDocument;
import learn.TrainWorkbench;
import matrix.Matrix;
import org.junit.Test;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by schreon on 10/31/14.
 */
public class TestPerformance {

    final static int minVecSize = 25;
    final static int maxVecSize = 400;
    final static int doc_size = 4096;
    final static int min_work_size = 1;
    final static int max_work_size = 64;
    final static long n = 100;
    final static long epochs = 10;

    @Test
    public void singleCore() {
        final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");

        final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), minVecSize);
        final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), minVecSize);

        syn0.initDirectBuffer();
        syn1.initDirectBuffer();

        syn0.fillNormal(0.0, 0.01);
        syn1.fillNormal(0.0, 0.01);

        int max_idx = vocabulary.getNumVocables();

        TrainWorkbench workBench = new TrainWorkbench(minVecSize, syn0, syn1);

        // single core :)
        double maxWordsSec = 0;
        for (int it=0; it < epochs; it++) {
            // Always set the same seed for the random number generator
            Random random = new  Random();
            random.setSeed(1234);

            Vocable[] words = new Vocable[doc_size];
            for (int i=0; i < doc_size; i++) {
                words[i] = vocabulary.get(vocabulary.getWordByIndex(random.nextInt(max_idx)));
            }
            long end;
            long start = System.nanoTime();
            for (int i=0; i < n; i++) {
                TrainDocument.computeDirectly(workBench, words, 0, doc_size, 0.001f);
            }
            end = System.nanoTime();
            double words_per_sec = (double) doc_size * n * 1000000.0 / (end - start);
            maxWordsSec = Math.max(words_per_sec, maxWordsSec);
            System.out.println(words_per_sec);
        }
        System.out.printf("    single-core, max words/sec: %-4f %n", maxWordsSec);
    }

    @Test
    public void benchmark() {
        System.out.println("Oh mon dieu!");
        final Vocabulary vocabulary = Vocabulary.loadFromFile("vocabulary.bin");

        for (int vecSize=minVecSize; vecSize <= maxVecSize; vecSize = 2*vecSize) {

            System.out.println();
            System.out.printf("--- vector size %d --- %n", vecSize);

            final Matrix syn0 = new Matrix(vocabulary.getNumVocables(), vecSize);
            final Matrix syn1 = new Matrix(vocabulary.getNumNodes(), vecSize);

            syn0.initDirectBuffer();
            syn1.initDirectBuffer();

            syn0.fillNormal(0.0, 0.01);
            syn1.fillNormal(0.0, 0.01);

            final int finalVecSize = vecSize;
            ObjectPool<TrainWorkbench> workBenchPool = new ObjectPool<TrainWorkbench>(doc_size) {
                @Override
                protected TrainWorkbench createObject() {
                    return new TrainWorkbench(finalVecSize, syn0, syn1);
                }
            };

            int max_idx = vocabulary.getNumVocables();

            double maxWordsSec;

            ForkJoinPool pool = new ForkJoinPool();

            for (int work_size=min_work_size; work_size <= max_work_size; work_size = work_size*2) {


                TrainDocument.LIMIT = max_work_size;

                maxWordsSec = 0.0;
                for (int it=0; it < epochs; it++) {
                    // Always set the same seed for the random number generator
                    Random random = new  Random();
                    random.setSeed(1234);

                    Vocable[] words = new Vocable[doc_size];
                    for (int i=0; i < doc_size; i++) {
                        words[i] = vocabulary.get(vocabulary.getWordByIndex(random.nextInt(max_idx)));
                    }
                    long end;
                    long start = System.nanoTime();
                    for (int i=0; i < n; i++) {
                        pool.invoke(new TrainDocument(workBenchPool, words, 0, doc_size, 0.001f));
                    }
                    end = System.nanoTime();
                    double words_per_sec = (double) doc_size * n * 1000000.0 / (end - start);
                    maxWordsSec = Math.max(words_per_sec, maxWordsSec);
                }
                System.out.printf("    work-size: %d, max words/sec: %-4f %n", work_size, maxWordsSec);
            }

            // single core
            maxWordsSec = 0.0;
            TrainWorkbench workBench = new TrainWorkbench(vecSize, syn0, syn1);
            for (int it=0; it < epochs; it++) {
                // Always set the same seed for the random number generator
                Random random = new  Random();
                random.setSeed(1234);

                Vocable[] words = new Vocable[doc_size];
                for (int i=0; i < doc_size; i++) {
                    words[i] = vocabulary.get(vocabulary.getWordByIndex(random.nextInt(max_idx)));
                }
                long end;
                long start = System.nanoTime();
                for (int i=0; i < n; i++) {
                    TrainDocument.computeDirectly(workBench, words, 0, doc_size, 0.001f);
                }
                end = System.nanoTime();
                double words_per_sec = (double) doc_size * n * 1000000.0 / (end - start);
                maxWordsSec = Math.max(words_per_sec, maxWordsSec);
            }
            System.out.printf("    single-core, max words/sec: %-4f %n", maxWordsSec);
        }
    }
}
