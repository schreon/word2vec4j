import java.io.Serializable;
import java.util.List;

public class Vocable implements Serializable {
    private int index;
    private long count;
    private List<Integer> path;

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getCount() {
        return count;
    }

    public synchronized void increaseCount() {
        count += 1;
    }
}
