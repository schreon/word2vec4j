package learn;

import matrix.Matrix;
import vocabulary.Vocable;
import vocabulary.Vocabulary;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

public class TrainSentence extends RecursiveTask<Integer> {

    public static final int LIMIT = 512;
    final static String empty = "".intern();
    final static int wSize = 5;
    final Matrix syn0;
    final Matrix syn1;
    final String[] tokens;
    final Vocabulary vocabulary;
    final int start;
    final int end;
    final float alpha;

    public TrainSentence(Matrix syn0, Matrix syn1, String[] tokens, Vocabulary vocabulary, int start, int end, float alpha) {
        this.syn0 = syn0;
        this.syn1 = syn1;
        this.tokens = tokens;
        this.vocabulary = vocabulary;
        this.start = start;
        this.end = end;
        this.alpha = alpha;
    }

    public static float logistic(final float f) {
        return 1.0f / (1.0f + (float) Math.exp(-Math.max(-45.0f, Math.min(f, 45.f))));
    }

    public static void trainPair(final Matrix syn0, final Matrix syn1, final int wordIndex, final int[] path, final boolean[] code, final float[] work, final float alpha) {
        int a, b;
        int syn1idx;
        float f, g;
        for (a = 0; a < syn0.m; a++) {
            work[a] = 0.0f;
        }
        for (b = 0; b < path.length; b++) {
            syn1idx = path[b];
            f = 0.0f;
            for (a = 0; a < syn0.m; a++) {
                f += syn0.get(wordIndex, a) * syn1.get(syn1idx, a);
            }
            f = logistic(f);
            if (code[b]) {
                // code is 1.0f
                g = -f * alpha;
            } else {
                // code is 0.0f
                g = (1.0f - f) * alpha;
            }
            for (a = 0; a < syn0.m; a++) {
                work[a] += g * syn1.get(syn1idx, a);
                syn1.add(syn1idx, a, g * syn0.get(wordIndex, a));
            }
        }
        for (a = 0; a < syn0.m; a++) {
            syn0.add(wordIndex, a, work[a]);
        }
    }

    public Integer computeDirectly(Matrix syn0, Matrix syn1, String[] tokens, Vocabulary vocabulary, int start, int end, float alpha) {
        String token;
        Vocable word;
        Vocable other;
        int wordIndex;
        int upperBound = tokens.length - 1;
        int rightBound;
        int reducedWindow;
        final float[] work = new float[syn0.m];
        for (int i = start; i < end; i++) {
            token = tokens[i].intern();
            if (token != empty) {
                reducedWindow = ThreadLocalRandom.current().nextInt(wSize) + 1;
                rightBound = Math.min(upperBound, i + reducedWindow);
                word = vocabulary.get(token);
                wordIndex = word.getIndex();
                for (int w = i + 1; w < rightBound; w++) {
                    token = tokens[w].intern();
                    if (token != empty) {
                        other = vocabulary.get(token);
                        trainPair(syn0, syn1, wordIndex, other.getPath(), other.getCode(), work, alpha);
                        trainPair(syn0, syn1, other.getIndex(), word.getPath(), word.getCode(), work, alpha);
                    }
                }
            }
        }
        return (end - start);
    }

    @Override
    protected Integer compute() {
        int diff = end - start;
        if (diff <= LIMIT) {
            return computeDirectly(syn0, syn1, tokens, vocabulary, start, end, alpha);
        } else {
            int split;
            // Try to make big chunks
            if (diff < 2 * LIMIT) {
                split = start + LIMIT;
            } else {
                split = (start + end) / 2;
            }
            TrainSentence left = new TrainSentence(syn0, syn1, tokens, vocabulary, start, split, alpha);
            TrainSentence right = new TrainSentence(syn0, syn1, tokens, vocabulary, split, end, alpha);
            invokeAll(left, right);
            try {
                return left.get() + right.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}