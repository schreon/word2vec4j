package vocabulary;

import java.util.*;

class HuffmanLeaf implements HuffmanElement {
    private Vocable vocable;

    public HuffmanLeaf(Vocable vocable) {
        this.vocable = vocable;
    }

    @Override
    public long getCount() {
        return vocable.getCount();
    }

    @Override
    public void encodePath(List<Integer> parentPath) {
        vocable.setPath(parentPath);
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

    public static Vocabulary createVocabulary(Map<String, Integer> wordCount) {
        System.out.println("Creating vocabulary ...");
        int n = wordCount.size();
        PriorityQueue<HuffmanElement> p = new PriorityQueue<>(n, new Comparator<HuffmanElement>() {
            @Override
            public int compare(HuffmanElement o1, HuffmanElement o2) {
                long c1 = o1.getCount();
                long c2 = o2.getCount();

                if (c1 < c2) return -1;
                if (c1 > c2) return 1;
                return 0;
            }
        });
        int syn0idx = 0;
        int syn1idx = 0;
        Iterator<Map.Entry<String, Integer>> iter = wordCount.entrySet().iterator();
        Vocabulary vocabulary = new Vocabulary();
        Map.Entry<String, Integer> entry;
        while (iter.hasNext()) {
            entry = iter.next();
            Vocable vocable = new Vocable();
            vocable.setCount(entry.getValue());
            vocable.setIndex(syn0idx);
            vocabulary.put(entry.getKey(), vocable);
            p.add(new HuffmanLeaf(vocable));
            syn0idx += 1;
            iter.remove();
        }

        System.out.printf("Creating leaves%n");
        HuffmanElement left, right;
        while (p.size() > 1) {
            left = p.poll();
            right = p.poll();
            p.add(new HuffmanTree(left.getCount() + right.getCount(), syn1idx, left, right));
            syn1idx += 1;
        }
        HuffmanElement root = p.poll();

        System.out.printf("Creating inner nodes%n");

        List<List<Integer>> paths = new ArrayList<>(syn0idx);

        int s = wordCount.size();
        for (int i = 0; i < s; i++) {
            paths.add(null);
        }

        System.out.printf("Encoding paths %n");
        root.encodePath(new ArrayList<Integer>());

        vocabulary.setNum_vocables(syn0idx);
        vocabulary.setNum_nodes(syn1idx);
        return vocabulary;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void encodePath(List<Integer> parentPath) {
        List<Integer> leftPath = new ArrayList<>(parentPath.size());
        leftPath.addAll(parentPath);
        leftPath.add(index);
        parentPath.add(index); // reuse this object
        left.encodePath(leftPath);
        right.encodePath(parentPath);
    }
}
