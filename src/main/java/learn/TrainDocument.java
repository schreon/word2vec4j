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
        for (int a = 0; a < vec.length; a++) {
            vec[a] = 0.0f;
        }
    }

    /**
     * Calculates the gradient for one element in the target word's path.
     *
     * @param wb the current workbench object
     */
    public static void trainPath(TrainWorkbench wb) {
        wb.syn1buf.position(wb.path[wb.b] * wb.vectorLength);
        wb.syn1buf.get(wb.syn1);

        wb.layer1 = 0.0f;

        for (wb.i = 0; wb.i < wb.vectorLength; wb.i++) {
            wb.layer1 += wb.syn0[wb.i] * wb.syn1[wb.i];
        }

        // logistic transfer function
        wb.layer1 = logistic(wb.layer1);

        if (wb.code[wb.b]) {
            wb.gradient = -wb.layer1 * wb.alpha;
        } else {
            wb.gradient = (1.0f - wb.layer1) * wb.alpha;
        }

        for (wb.i = 0; wb.i < wb.vectorLength; wb.i++) {
            wb.layer0[wb.i] += wb.gradient * wb.syn1[wb.i];
            wb.syn1[wb.i] += wb.gradient * wb.syn0[wb.i];
        }
        wb.syn1buf.position(wb.path[wb.b] * wb.vectorLength);
        wb.syn1buf.put(wb.syn1);
    }

    /**
     * Iterates over the target word's path and calls trainPath which actually computes each probability.
     *
     * @param wb the current workbench object
     */
    public static void trainOther(TrainWorkbench wb) {
        wb.otherWord = wb.words[wb.otherPos];
        if ((wb.pos != wb.otherPos) && (wb.otherWord != null)) {
            wb.path = wb.otherWord.getPath();
            wb.code = wb.otherWord.getCode();

            fillZero(wb.layer0);

            for (wb.b = 0; wb.b < wb.path.length; wb.b++) {
                trainPath(wb);
            }
            for (wb.i = 0; wb.i < wb.vectorLength; wb.i++) {
                wb.syn0[wb.i] += wb.layer0[wb.i];
            }
            wb.syn0buf.position(wb.wordIndex * wb.vectorLength);
            wb.syn0buf.put(wb.syn0);
        }

    }

    /**
     * Trains the word at the current position. Randomizes the window and chooses the words left and right of the
     * center word. Calls trainOther which computes the training step between the pair.
     *
     * @param wb the current workbench object
     */
    public static void trainWord(TrainWorkbench wb) {
        wb.word = wb.words[wb.pos];
        if (wb.word == null) return;
        wb.wordIndex = wb.word.getIndex();
        wb.syn0buf.position(wb.wordIndex * wb.vectorLength);
        wb.syn0buf.get(wb.syn0);

        wb.reducedWindow = ThreadLocalRandom.current().nextInt(wSize);
        wb.leftBound = Math.max(0, wb.pos - wSize + wb.reducedWindow);
        wb.rightBound = Math.min(wb.words.length, wb.pos + wSize + 1 - wb.reducedWindow);
        for (wb.otherPos = wb.leftBound; wb.otherPos < wb.rightBound; wb.otherPos++) {
            trainOther(wb);
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
        wb.alpha = alpha;
        wb.n = 0;
        for (wb.pos = start; wb.pos < end; wb.pos++) {
            trainWord(wb);
            wb.n++;
        }
        return wb.n;
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
