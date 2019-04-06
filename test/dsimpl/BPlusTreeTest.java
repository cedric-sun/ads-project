package dsimpl;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class BPlusTreeTest {
    static class Pair {
        final int k;
        final double v;

        public Pair(int k, double v) {
            this.k = k;
            this.v = v;
        }
    }

    static Pair[] randomPairArray(int N) {
        class FisherYatesShuffler {
            void shuffle(Pair[] ar) {
                // If running on Java 6 or older, use `new Random()` on RHS here
                Random rnd = ThreadLocalRandom.current();
                for (int i = ar.length - 1; i > 0; i--) {
                    int index = rnd.nextInt(i + 1);
                    // Simple swap
                    Pair a = ar[index];
                    ar[index] = ar[i];
                    ar[i] = a;
                }
            }
        }
        Random random = new Random();
        Pair[] ret = new Pair[N];
        for (int i = 0; i < N; i++) {
            ret[i] = new Pair(i, random.nextDouble());
        }
        new FisherYatesShuffler().shuffle(ret);
        return ret;
    }

    @Test
    public void randomBulkInsertAndGet() {
        final int M = 20, N = 10000;
        BPlusTree bPlusTree = new BPlusTree(M);
        Pair[] testData = randomPairArray(N);
        for (Pair pair : testData)
            bPlusTree.insert(pair.k, pair.v);

        for (Pair pair : testData)
            assertTrue(bPlusTree.get(pair.k) == pair.v);
    }

    final Pair[] simpleTestData = new Pair[]{
            new Pair(9, 0.7258506976568656),
            new Pair(1, 0.25137529047540985),
            new Pair(8, 0.436500663475739),
            new Pair(4, 0.2963288216690265),
            new Pair(5, 0.6161846898229543),
            new Pair(7, 0.056058099068894474),
            new Pair(2, 0.1688243376166424),
            new Pair(0, 0.46375381209298183),
            new Pair(3, 0.6122986978235035),
            new Pair(6, 0.4837472857372376)
    };

    @Test
    public void simpleInsertAndGet() {

        BPlusTree bp = new BPlusTree(3);
        for (Pair pair : simpleTestData)
            bp.insert(pair.k, pair.v);

        for (Pair pair : simpleTestData)
            assertTrue(bp.get(pair.k) == pair.v);
    }

    @Test
    public void simpleInsertAndDelete() {
        BPlusTree bp = new BPlusTree(3);
        for (Pair pair : simpleTestData)
            bp.insert(pair.k, pair.v);

        for (Pair pair : simpleTestData)
            bp.delete(pair.k);
    }

    @Test
    public void randomBulkInsertAndDelete() {
        final int M = 20, N = 10000;
        BPlusTree bPlusTree = new BPlusTree(M);
        Pair[] testData = randomPairArray(N);
        for (Pair pair : testData)
            bPlusTree.insert(pair.k, pair.v);

        for (Pair pair : testData)
            bPlusTree.delete(pair.k);
    }

    Pair[] simpleTestData2 = new Pair[]{
            new Pair(2, 0.784514), new Pair(1, 0.523413),
            new Pair(3, 0.947660), new Pair(10, 0.805863),
            new Pair(11, 0.234790), new Pair(19, 0.301759),
            new Pair(8, 0.988133), new Pair(0, 0.911702),
            new Pair(14, 0.706333), new Pair(16, 0.250606),
            new Pair(6, 0.161544), new Pair(7, 0.335414),
            new Pair(13, 0.506242), new Pair(17, 0.317764),
            new Pair(5, 0.277398), new Pair(9, 0.054883),
            new Pair(18, 0.511996), new Pair(4, 0.773309),
            new Pair(12, 0.534230), new Pair(15, 0.562825)
    };

    @Test
    public void simpleInsertAndDelete2() {
        BPlusTree bPlusTree = new BPlusTree(3);
        for (Pair pair : simpleTestData2)
            bPlusTree.insert(pair.k, pair.v);
        for (Pair pair : simpleTestData2)
            bPlusTree.delete(pair.k);
    }

    @Test
    public void range() {
        final int M = 20, N = 10000;
        BPlusTree bp = new BPlusTree(M);
        Pair[] testData = randomPairArray(N);
        for (Pair pair : testData) {
            bp.insert(pair.k, pair.v);
        }
        Arrays.sort(testData, Comparator.comparingInt(pair -> pair.k));
        Random random = new Random();
        //testData: k in [0,N-1]
        int l = random.nextInt(N), r = random.nextInt(N);
        if (l > r) {
            int tmp = l;
            l = r;
            r = tmp;
        }
        int cnt = r - l + 1;
        double[] expected = new double[cnt];
        for (int i = 0; i < cnt; i++) {
            expected[i] = testData[l + i].v;
        }
        double[] actual = bp.range(l, r);
        assertArrayEquals(expected, actual, 0);
    }
}