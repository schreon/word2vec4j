package learn;

import common.ObjectPool;
import vocabulary.Vocable;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Recursive task which trains the given neural network weight matrices given a document consisting of words.
 */
public class TrainDocument extends RecursiveTask<Integer> {

    public static int LIMIT;
    public final static int wSize = 5;

    final int start;
    final int end;
    final float alpha;
    final ObjectPool<TrainWorkbench> wbPool;
    final Vocable[] words;

    /**
     * @param wbPool A workbench pool.
     * @param words  The complete word array.
     * @param start  The starting index.
     * @param end    The ending index.
     * @param alpha  The current learning rate.
     */
    public TrainDocument(ObjectPool<TrainWorkbench> wbPool, final Vocable[] words, int start, int end, float alpha) {
        this.wbPool = wbPool;
        this.words = words;
        this.start = start;
        this.end = end;
        this.alpha = alpha;
    }

    /**
     * Computes the logistic sigmoid transfer function.
     * See http://en.wikipedia.org/wiki/Logistic_function
     *
     * @param f the net output
     * @return the transformed net output
     */
    public static float logistic(float f) {
        return 1.0f / (1.0f + (float) Math.exp(-Math.max(-45.0f, Math.min(f, 45.0f))));
    }

    /**
     * Fills the given vector with zeros.
     *
     * @param vec the vector to fill with zeros.
     */
    public static void fillZero(float[] vec) {

    }

    /**
     * Trains the word at the current position.
     *
     * This is main hot spot during training. Any optimization effort should be put here.
     *
     * @param wb the current workbench object
     */
    public static void trainWord(int pos, float alpha, TrainWorkbench wb) {
        wb.word = wb.words[pos];
        if (wb.word == null) return;
        int wordIndex = wb.word.getIndex();
        wb.syn0buf.position(wordIndex * wb.vectorLength);
        wb.syn0buf.get(wb.syn0);

        int a, b, i;
        float net_output, delta;

        int reducedWindow = ThreadLocalRandom.current().nextInt(wSize);
        int leftBound = Math.max(0, pos - wSize + reducedWindow);
        int rightBound = Math.min(wb.words.length, pos + wSize + 1 - reducedWindow);
        for (int otherPos = leftBound; otherPos < rightBound; otherPos++) {
            wb.otherWord = wb.words[otherPos];
            if ((pos != otherPos) && (wb.otherWord != null)) {
                wb.path = wb.otherWord.getPath();
                wb.code = wb.otherWord.getCode();

                for (a = 0; a < wb.layer0.length; a++) {
                    wb.layer0[a] = 0.0f;
                }

                for (b = 0; b < wb.path.length; b++) {
                    wb.syn1buf.position(wb.path[b] * wb.vectorLength);
                    wb.syn1buf.get(wb.syn1);

                    net_output = 0.0f;

                    for (i = 0; i < wb.vectorLength; i++) {
                        net_output += wb.syn0[i] * wb.syn1[i];
                    }

                    // logistic transfer function
                    net_output = logistic(net_output);

                    if (wb.code[b]) {
                        delta = -net_output * alpha;
                    } else {
                        delta = (1.0f - net_output) * alpha;
                    }

                    for (i = 0; i < wb.vectorLength; i++) {
                        wb.layer0[i] += delta * wb.syn1[i];
                        wb.syn1[i] += delta * wb.syn0[i];
                    }
                    wb.syn1buf.position(wb.path[b] * wb.vectorLength);
                    wb.syn1buf.put(wb.syn1);
                }

                for (i = 0; i < wb.vectorLength; i++) {
                    wb.syn0[i] += wb.layer0[i];
                }
                wb.syn0buf.position(wordIndex * wb.vectorLength);
                wb.syn0buf.put(wb.syn0);
            }
        }
    }

    /**
     * Iterates over an array of words and trains the neural network on them.
     *
     * @param wb    the current workbench object
     * @param words the complete string of words
     * @param start the starting index for this worker
     * @param end   the end index for this worker
     * @param alpha the current learningrate
     * @return the number of words that have actually been calculated (omitting out of vocabulary words)
     */
    public static Integer computeDirectly(TrainWorkbench wb, Vocable[] words, int start, int end, float alpha) {
        wb.words = words;
        int n = 0;
        for (int pos = start; pos < end; pos++) {
            trainWord(pos, alpha, wb);
            n++;
        }
        return n;
    }

    /**
     * If the number of words is larger than LIMIT, the computation is forked in 2 halves. If it is smaller or equal,
     * the computation is processed directly.
     *
     * @return number of trained words (without out-of-vocabulary words)
     */
    @Override
    protected Integer compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            TrainWorkbench wb = wbPool.borrowObject();
            int res = computeDirectly(wb, words, start, end, alpha);
            wbPool.returnObject(wb);
            return res;
        } else {
            int split = (start + end) / 2;
            TrainDocument left = new TrainDocument(wbPool, words, start, split, alpha);
            TrainDocument right = new TrainDocument(wbPool, words, split, end, alpha);
            invokeAll(left, right);
            try {
                return left.get() + right.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
