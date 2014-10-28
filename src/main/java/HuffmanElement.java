import java.util.List;

public interface HuffmanElement {
    public long getCount();

    public void encodePath(List<Integer> parentPath);
}
