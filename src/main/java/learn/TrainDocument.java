package learn;

import common.ObjectPool;
import vocabulary.Vocable;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

public class TrainDocument extends RecursiveTask<Integer> {

    public static int LIMIT;
    public final static int wSize = 5;

    final int start;
    final int end;
    final float alpha;
    final ObjectPool<TrainWorkbench> wbPool;
    final Vocable[] words;

    public TrainDocument(ObjectPool<TrainWorkbench> wbPool, final Vocable[] words, int start, int end, float alpha) {
        this.wbPool = wbPool;
        this.words = words;
        this.start = start;
        this.end = end;
        this.alpha = alpha;
    }

    public static float logistic(float f) {
        return 1.0f / (1.0f + (float) Math.exp(-Math.max(-45.0f, Math.min(f, 45.0f))));
    }

    public static void fillZero(float[] vec) {
        for (int a=0; a < vec.length; a++) {
            vec[a] = 0.0f;
        }
    }

    public static void trainPath(TrainWorkbench wb) {
        wb.syn1buf.position(wb.path[wb.b]*wb.vectorLength);
        wb.syn1buf.get(wb.syn1);

        wb.layer1 = 0.0f;

        for (wb.i=0; wb.i < wb.vectorLength; wb.i++) {
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
        wb.syn1buf.position(wb.path[wb.b]*wb.vectorLength);
        wb.syn1buf.put(wb.syn1);
    }

    public static void trainOther(TrainWorkbench wb) {
        wb.otherWord = wb. words[wb.otherPos];
        if ((wb.pos != wb.otherPos) && (wb.otherWord != null)) {
            wb.path = wb.otherWord.getPath();
            wb.code = wb.otherWord.getCode();

            fillZero(wb.layer0);

            for (wb.b = 0; wb.b < wb.path.length; wb.b++) {
                trainPath(wb);
            }
            for (wb.i=0; wb.i < wb.vectorLength; wb.i++) {
                wb.syn0[wb.i] += wb.layer0[wb.i];
            }
            wb.syn0buf.position(wb.wordIndex*wb.vectorLength);
            wb.syn0buf.put(wb.syn0);
        }

    }

    public static void trainWord(TrainWorkbench wb) {
        wb.word = wb.words[wb.pos];
        if (wb.word == null) return;
        wb.wordIndex = wb.word.getIndex();
        wb.syn0buf.position(wb.wordIndex*wb.vectorLength);
        wb.syn0buf.get(wb.syn0);

        wb.reducedWindow = ThreadLocalRandom.current().nextInt(wSize);
        wb.leftBound = Math.max(0, wb.pos - wSize + wb.reducedWindow);
        wb.rightBound = Math.min(wb.words.length, wb.pos + wSize + 1 - wb.reducedWindow);
        for (wb.otherPos = wb.leftBound; wb.otherPos < wb.rightBound; wb.otherPos++) {
            trainOther(wb);
        }
    }

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
