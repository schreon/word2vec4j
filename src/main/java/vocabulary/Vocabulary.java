package vocabulary;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Vocabulary extends HashMap<String, Vocable> implements Serializable {

    private int num_vocables;
    private int num_nodes;

    public static Vocabulary loadFromFile(String fileName) {
        try {
            FileInputStream fi = new FileInputStream(fileName);
            ObjectInputStream is = new ObjectInputStream(fi);
            Vocabulary vocabulary = (Vocabulary) is.readObject();
            is.close();
            fi.close();
            return vocabulary;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public void saveToFile(String fileName) {
        try {

            FileOutputStream fo = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(this);
            os.close();
            fo.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getWordByIndex(int index) {
        for (Map.Entry<String, Vocable> entry : this.entrySet()) {
            if (entry.getValue().getIndex() == index) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No word found for index " + index);
    }
}
