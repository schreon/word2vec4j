import java.io.Serializable;
import java.util.HashMap;

public class Vocabulary extends HashMap<String, Vocable> implements Serializable {

    private int num_vocables;
    private int num_nodes;

    public int getNumVocables() {
        return num_vocables;
    }

    public void setNum_vocables(int num_vocables) {
        this.num_vocables = num_vocables;
    }

    public int getNumNodes() {
        return num_nodes;
    }

    public void setNum_nodes(int num_nodes) {
        this.num_nodes = num_nodes;
    }
}
