package vocabulary;

import java.io.Serializable;

public class Vocable implements Serializable {
    private int index;
    private int count;
    private int[] path;
    private boolean[] code;

    public int[] getPath() {
        return path;
    }

    public void setPath(final int[] path) {
        this.path = path;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean[] getCode() {
        return code;
    }

    public void setCode(boolean[] code) {
        this.code = code;
    }
}
