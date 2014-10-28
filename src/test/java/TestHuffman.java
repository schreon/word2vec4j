import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by schreon on 10/27/14.
 */
public class TestHuffman {
    @Test
    public void testHuffman() throws Exception {

        FileInputStream fi = new FileInputStream("wordcount.bin");
        ObjectInputStream is = new ObjectInputStream(fi);
        Map<String, Integer> wordCount = (ConcurrentHashMap<String, Integer>) is.readObject();
        is.close();
        fi.close();

        Vocabulary vocabulary = HuffmanTree.createVocabulary(wordCount);

        for (String word : Arrays.asList("der", "die", "wurst")) {
            System.out.println(word);
            System.out.println(vocabulary.get(word).getIndex());
            System.out.println(vocabulary.get(word).getCount());
            System.out.println(vocabulary.get(word).getPath());
        }

        System.out.printf("Total of %d vocables %n", vocabulary.getNumVocables());
        System.out.printf("Total of %d nodes %n", vocabulary.getNumNodes());
        FileOutputStream fo = new FileOutputStream("vocabulary.bin");
        ObjectOutputStream os = new ObjectOutputStream(fo);
        os.writeObject(vocabulary);
        os.close();
        fo.close();

        System.out.println("Finished");
    }
}
