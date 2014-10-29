package vocabulary;

public interface HuffmanElement {
    public long getCount();

    public void encodePath(int[] parentPath, boolean[] code);
}
