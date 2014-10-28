package matrix;

/**
 * Created by schreon on 10/29/14.
 */
public class Similarity implements Comparable<Similarity> {

    public final float similarity;
    public final int index;

    Similarity(final float similarity, final int index) {
        this.similarity = similarity;
        this.index = index;
    }

    @Override
    public int compareTo(Similarity o) {
        return -Float.compare(o.similarity, similarity);
    }

    @Override
    public String toString() {
        return "[" +
                index +
                " : " + similarity +
                ']';
    }
}
