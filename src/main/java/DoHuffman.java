import vocabulary.HuffmanTree;
import vocabulary.Vocabulary;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoHuffman {
    public static void main(String[] args) {
        try {
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
            vocabulary.saveToFile("vocabulary.bin");

            System.out.println("Finished");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
