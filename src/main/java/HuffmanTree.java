import java.util.*;

class HuffmanLeaf implements HuffmanElement, Comparable<HuffmanElement> {
    public long count;
    public String word;
    public int index;
    public HuffmanLeaf(long count, String word, int index) {
        this.count = count;
        this.word = word;
        this.index = index;
    }

    @Override
    public int compareTo(HuffmanElement o) {
        return count < o.getCount() ? -1 : 1;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public void encodePath(List<Integer> parentPath, List<List<Integer>> paths) {
        paths.set(index, parentPath);
    }

}

public class HuffmanTree implements HuffmanElement {

    private final long count;
    private final int index;
    private final HuffmanElement left;
    private final HuffmanElement right;

    public HuffmanTree(long count, int index, HuffmanElement left, HuffmanElement right) {
        this.count = count;
        this.index = index;
        this.left = left;
        this.right = right;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void encodePath(List<Integer> parentPath, List<List<Integer>> paths) {
        List<Integer> leftPath = new ArrayList<>(parentPath.size());
        leftPath.addAll(parentPath);
        leftPath.add(index);
        parentPath.add(index); // reuse this object
        left.encodePath(leftPath, paths);
        right.encodePath(parentPath, paths);
    }


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
            p.add(new HuffmanTree(left.getCount()+right.getCount(), syn1idx, left, right));
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
