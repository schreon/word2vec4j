import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public interface HuffmanElement {
    public long getCount();
    public void encodePath(List<Integer> parentPath,
                           List<List<Integer>> paths);

    public static HuffmanElement createTree(Map<String, Vocable> wordCount) {
        PriorityQueue<HuffmanElement> p = new PriorityQueue <>();
        int syn0idx = 0;
        int syn1idx = 0;
        long count;
        String word;
        for (Map.Entry<String, Vocable> entry : wordCount.entrySet()) {
            word = entry.getKey();
            count = entry.getValue().getCount();
            p.add(new HuffmanLeaf(count, word, syn0idx));
            syn0idx += 1;
        }

        HuffmanElement left, right;
        while (p.size() > 1) {
            left = p.poll();
            right = p.poll();
            p.add(new HuffmanNode(left.getCount()+right.getCount(), syn1idx, left, right));
            syn1idx += 1;
        }

        return p.poll();
    }

    public static List<List<Integer>> createPaths(Map<String, Vocable> wordCount) {
        HuffmanElement root = createTree(wordCount);
        List<List<Integer>> paths = new ArrayList<>(wordCount.size());
        root.encodePath(new ArrayList<Integer>(), paths);
        return paths;
    }
}
