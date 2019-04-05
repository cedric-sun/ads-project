package dsimpl;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class BPlusTreeTest {
    static void shuffleArray(int[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @Test
    public void insertAndGet() {
        final int M = 20, N = 10000;
        Random random = new Random();
        Map<Integer, Double> goodMap = new HashMap<>();
        BPlusTree bPlusTree = new BPlusTree(M);
        int[] scatter = new int[N];
        for (int i = 0; i < N; i++) scatter[i] = i;
        shuffleArray(scatter);
        for (int i = 0; i < N; i++) {
            double v = random.nextDouble();
            goodMap.putIfAbsent(scatter[i], v);
            bPlusTree.insert(scatter[i], v);
        }

        for (Map.Entry<Integer, Double> entry : goodMap.entrySet()) {
            int k = entry.getKey();
            double expected = entry.getValue();
            double actual = bPlusTree.get(k);
//            System.out.println(String.format("key: %d, expected: %f, actual: %f", k, expected, actual));
            assertTrue(actual == expected);
        }
    }

    @Test
    public void insertAndGet2() {
        class Pair {
            int k;
            double v;

            public Pair(int k, double v) {
                this.k = k;
                this.v = v;
            }
        }
        Pair[] data = new Pair[]{
                new Pair(9,0.7258506976568656),
                new Pair(1,0.25137529047540985),
                new Pair(8,0.436500663475739),
                new Pair(4,0.2963288216690265),
                new Pair(5,0.6161846898229543),
                new Pair(7,0.056058099068894474),
                new Pair(2,0.1688243376166424),
                new Pair(0,0.46375381209298183),
                new Pair(3,0.6122986978235035),
                new Pair(6,0.4837472857372376)
        };
        BPlusTree bp = new BPlusTree(3);
        for (int i = 0; i < data.length; i++) {
            bp.insert(data[i].k,data[i].v);
        }
//        double enigma = bp.get(6);
//        System.out.println(enigma);
        for (int i = 0; i < data.length; i++) {
            assertTrue(bp.get(data[i].k) == data[i].v);
        }
    }

    @Test
    public void delete() {
        class Pair {
            int k;
            double v;

            public Pair(int k, double v) {
                this.k = k;
                this.v = v;
            }
        }
        Pair[] data = new Pair[]{
                new Pair(9,0.7258506976568656),
                new Pair(1,0.25137529047540985),
                new Pair(8,0.436500663475739),
                new Pair(4,0.2963288216690265),
                new Pair(5,0.6161846898229543),
                new Pair(7,0.056058099068894474),
                new Pair(2,0.1688243376166424),
                new Pair(0,0.46375381209298183),
                new Pair(3,0.6122986978235035),
                new Pair(6,0.4837472857372376)
        };
        BPlusTree bp = new BPlusTree(3);
        for (int i = 0; i < data.length; i++) {
            bp.insert(data[i].k,data[i].v);
        }
        for (int i = 0; i < data.length; i++) {
            bp.delete(data[i].k);
        }
    }



    @Test
    public void get() {
    }

    @Test
    public void range() {
        class Pair {
            int k;
            double v;

            public Pair(int k, double v) {
                this.k = k;
                this.v = v;
            }
        }
        Pair[] data = new Pair[]{
                new Pair(9,0.7258506976568656),
                new Pair(1,0.25137529047540985),
                new Pair(8,0.436500663475739),
                new Pair(4,0.2963288216690265),
                new Pair(5,0.6161846898229543),
                new Pair(7,0.056058099068894474),
                new Pair(2,0.1688243376166424),
                new Pair(0,0.46375381209298183),
                new Pair(3,0.6122986978235035),
                new Pair(6,0.4837472857372376)
        };
        BPlusTree bp = new BPlusTree(3);
        for (int i = 0; i < data.length; i++) {
            bp.insert(data[i].k,data[i].v);
        }
        for (double d : bp.range(3, 7)) {
            System.out.println(d);
        }
    }
}