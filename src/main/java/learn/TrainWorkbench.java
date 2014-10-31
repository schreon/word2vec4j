package learn;

import matrix.Matrix;
import vocabulary.Vocable;

import java.nio.FloatBuffer;

/**
 * A workbench object holding fields that are necessary during computation. Avoids frequent reallocation.
 */
public class TrainWorkbench {

    public final int vectorLength;
    public final float[] layer0;
    public final float[] syn0;
    public final float[] syn1;
    public final FloatBuffer syn0buf;
    public final FloatBuffer syn1buf;

    public boolean[] code;
    public int[] path;
    public int wordIndex;
    public int leftBound;
    public int rightBound;
    public int reducedWindow;
    public int n;
    public int pos, b, i;
    public float layer1, gradient;
    public int otherPos;

    public Vocable word;
    public Vocable otherWord;
    public Vocable[] words;
    public float alpha;

    public TrainWorkbench(int vectorLength, Matrix syn0matrix, Matrix syn1matrix) {

        this.vectorLength = vectorLength;
        syn1 = new float[vectorLength];
        syn0 = new float[vectorLength];
        layer0 = new float[vectorLength];
        syn0buf = syn0matrix.buffer.duplicate();
        syn1buf = syn1matrix.buffer.duplicate();
    }
}
