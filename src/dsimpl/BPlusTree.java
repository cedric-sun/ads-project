package dsimpl;

import java.util.*;
import java.util.stream.Collectors;

public class BPlusTree {
    static class Pair {
        final int k;
        final double v;

        Pair(int k, double v) {
            this.k = k;
            this.v = v;
        }
    }

    abstract class Node {
        NonLeafNode parent;

        public Node() {
            parent = null;
        }
    }

    /**
     * Abstraction for non-leaf nodes. Non-leaf node only serves as navigator
     * for searching on tree.
     */
    class NonLeafNode extends Node {

        private ArrayList<Integer> keys;
        private ArrayList<Node> children;

        NonLeafNode(List<Integer> keys, List<Node> children) {
            this.keys = new ArrayList<>(keys);
            this.children = new ArrayList<>(children);
        }

        /**
         * Create a NonLeafNode with only 1 navigating key {@code k}, with specified
         * left child and right child.
         *
         * @param k
         * @param leftChild
         * @param righChild
         */
        NonLeafNode(int k, Node leftChild, Node righChild) {
            keys = new ArrayList<>();
            children = new ArrayList<>();
            keys.add(k);
            children.add(leftChild);
            children.add(righChild);
        }

        /**
         * For a given search key {@code k}, return the promising child to which
         * the searching should descend.
         * @param k
         * @return
         */
        Node promisingChild(int k) {
            return children.get(upperBound(k));
        }

        private int upperBound(int k) {
            int l = 0, r = keys.size();
            while (l < r) {
                int m = (l + r) >> 1;
                if (keys.get(m) > k) r = m;
                else l = m + 1;
            }
            return l;
        }

        private int lowerBound(int k) {
            int l = 0, r = keys.size();
            while (l < r) {
                int m = (l + r) >> 1;
                if (keys.get(m) < k) l = m + 1;
                else r = m;
            }
            return l;
        }

        /**
         * Insert a new navigating key k with specified right child.
         *
         * @param k
         * @param rightChild
         */
        void insert(int k, Node rightChild) {
            int i = upperBound(k);
            keys.add(i, k);
            children.add(i + 1, rightChild);
            rightChild.parent = this;
            if (keys.size() > MAX_NODE_SIZE) {
                NonLeafNode newNonLeafNode = new NonLeafNode(
                        keys.subList(MIN_NODE_SIZE + 1, M),
                        children.subList(MIN_NODE_SIZE + 1, M + 1)
                );

                for (Node child : newNonLeafNode.children)
                    child.parent = newNonLeafNode;

                int mid = keys.get(MIN_NODE_SIZE);

                for (int j = 0; j < M - MIN_NODE_SIZE; j++) {
                    keys.remove(keys.size() - 1);
                    children.remove(children.size() - 1);
                }

                if (parent == null) {
                    root = parent = new NonLeafNode(mid, this, newNonLeafNode);
                    newNonLeafNode.parent = parent;//safe publication
                } else {
                    parent.insert(mid, newNonLeafNode);
                }
            }
        }

        /**
         * Delete the greatest key k' <= k ,and the right child of k'.
         *
         * @param k
         */
        void delete(int k) {
            int i = upperBound(k);
            int removedKey = keys.remove(i - 1);
            children.remove(i);

            if (this == root) {
                if (keys.size() < 1) {
                    children.get(0).parent = null;
                    root = children.get(0);
                }
            } else {
                if (keys.size() < MIN_NODE_SIZE) {
                    int iRightSep = parent.lowerBound(removedKey);
                    int iRightSib = iRightSep + 1;
                    int iLeftSib = iRightSep - 1;
                    if (iRightSib < parent.children.size()
                            && ((NonLeafNode) parent.children.get(iRightSib)).keys.size() > MIN_NODE_SIZE) {
                        //borrow from right
                        //todo: balancing borrow
                        NonLeafNode rightSib = (NonLeafNode) parent.children.get(iRightSib);
                        keys.add(parent.keys.set(iRightSep, rightSib.keys.remove(0)));
                        Node borrowedChild = rightSib.children.remove(0);
                        children.add(borrowedChild);
                        borrowedChild.parent = this;
                    } else if (iLeftSib >= 0 && ((NonLeafNode) parent.children.get(iLeftSib)).keys.size() > MIN_NODE_SIZE) {
                        //borrow from left
                        NonLeafNode leftSib = (NonLeafNode) parent.children.get(iLeftSib);
                        keys.add(0, parent.keys.set(iRightSep - 1, leftSib.keys.remove(leftSib.keys.size() - 1)));
                        Node borrowedChild = leftSib.children.remove(leftSib.children.size() - 1);
                        children.add(0, borrowedChild);
                        borrowedChild.parent = this;
                    } else if (iRightSib < parent.children.size()) {
                        // merge right sibling into this
                        NonLeafNode rightSib = (NonLeafNode) parent.children.get(iRightSib);
                        keys.add(parent.keys.get(iRightSep));
                        keys.addAll(rightSib.keys);
                        children.addAll(rightSib.children);
                        for (Node node : rightSib.children)
                            node.parent = this;
                        parent.delete(rightSib.keys.get(0));
                    } else if (iLeftSib >= 0) {
                        // merge this into left sibling
                        NonLeafNode leftSib = (NonLeafNode) parent.children.get(iLeftSib);
                        leftSib.keys.add(parent.keys.get(iRightSep - 1));
                        leftSib.keys.addAll(keys);
                        leftSib.children.addAll(children);
                        for (Node node : children)
                            node.parent = leftSib;
                        parent.delete(removedKey);
                    } else {
                        throw new Error("IMPOSSIBLE ERROR");
                    }
                }
            }
        }
    }

    /**
     * Abstraction for bottom layer nodes storing dictionary pairs.
     */
    class LeafNode extends Node {
        private LeafNode prev = null, next = null;
        private ArrayList<Pair> data;

        LeafNode() {
            data = new ArrayList<>();
        }

        LeafNode(List<Pair> data) {
            this.data = new ArrayList<>(data);
        }

        void insert(Pair pair) {
            int i = lowerBound(pair.k);
            if (i < data.size() && data.get(i).k == pair.k) return;//duplicate insertion
            data.add(i, pair);
            if (data.size() > MAX_NODE_SIZE) {
                LeafNode newLeaf = new LeafNode(data.subList(MIN_NODE_SIZE, M));
                for (int j = 0; j < M - MIN_NODE_SIZE; j++) {
                    data.remove(data.size() - 1);
                }
                if (next != null)
                    next.prev = newLeaf;
                newLeaf.next = next;
                newLeaf.prev = this;
                next = newLeaf;

                if (parent == null) {
                    root = parent = new NonLeafNode(newLeaf.data.get(0).k, this, newLeaf);
                    newLeaf.parent = parent;//safe publication
                } else {
                    parent.insert(newLeaf.data.get(0).k, newLeaf);
                }
            }
        }

        /**
         * Get the pair associated with k.
         *
         * @param k
         * @return the pair associated with key k, if exists; null otherwise
         */
        Pair get(int k) {
            int i = lowerBound(k);
            if (i < data.size() && data.get(i).k == k)
                return data.get(i);
            return null;
        }

        private int lowerBound(int k) {
            int l = 0, r = data.size();
            while (l < r) {
                int m = (l + r) >> 1;
                if (data.get(m).k < k) l = m + 1;
                else r = m;
            }
            return l;
        }

        private int upperBound(int k) {
            int l = 0, r = data.size();
            while (l < r) {
                int m = (l + r) >> 1;
                if (data.get(m).k > k) r = m;
                else l = m + 1;
            }
            return l;
        }

        void delete(int k) {
            int i = lowerBound(k);
            if (i >= data.size() || data.get(i).k != k)
                return;
            data.remove(i);
            if (this != root && data.size() < MIN_NODE_SIZE) {
                if (prev != null && prev.parent == parent && prev.data.size() > MIN_NODE_SIZE) {
                    // borrow from left sibling
                    data.add(0, prev.data.remove(prev.data.size() - 1));
                    parent.keys.set(parent.lowerBound(data.get(0).k), data.get(0).k);
                } else if (next != null && next.parent == parent && next.data.size() > MIN_NODE_SIZE) {
                    // borrow from right sibling
                    //todo:balancing borrow
                    data.add(next.data.remove(0));
                    parent.keys.set(parent.lowerBound(next.data.get(0).k) - 1, next.data.get(0).k);
                } else if (prev != null && prev.parent == parent) {
                    // merge this into leftSibling
                    prev.data.addAll(data);
                    if (next != null)
                        next.prev = prev;
                    prev.next = next;
                    parent.delete(k);
                } else if (next != null && next.parent == parent) {
                    //merge right sibling into this
                    data.addAll(next.data);
                    LeafNode originalRightSib = next;
                    if (next.next != null)
                        next.next.prev = this;
                    next = next.next;
                    parent.delete(originalRightSib.data.get(0).k);
                } else {
                    throw new Error("IMPOSSIBLE ERROR");
                }
            }
        }

        List<Double> between(int l, int r) {
            return data.subList(lowerBound(l), upperBound(r))
                    .stream().map(pair -> pair.v).collect(Collectors.toList());
        }
    }


    private final int M;
    private final int MAX_NODE_SIZE;
    private final int MIN_NODE_SIZE;

    private Node root;

    public BPlusTree(int m) {
        M = m;
        MAX_NODE_SIZE = M - 1;
        MIN_NODE_SIZE = ((M & 1) == 1 ? (M + 1) >> 1 : M >> 1) - 1;
        root = new LeafNode();
    }

    public void insert(int k, double v) {
        Node node = root;
        while (!(node instanceof LeafNode)) {
            node = ((NonLeafNode) node).promisingChild(k);
        }
        ((LeafNode) node).insert(new Pair(k, v));
    }

    public void delete(int k) {
        Node node = root;
        while (!(node instanceof LeafNode)) {
            node = ((NonLeafNode) node).promisingChild(k);
        }
        ((LeafNode) node).delete(k);
    }

    public double get(int k) {
        Node node = root;
        while (!(node instanceof LeafNode))
            node = ((NonLeafNode) node).promisingChild(k);
        Pair pair = ((LeafNode) node).get(k);
        if (pair == null) return Double.NaN;
        return pair.v;
    }

    // l <= k <= r
    public double[] range(int l, int r) {
        Node lNode = root, rNode = root;
        ArrayList<Double> ans = new ArrayList<>();

        while (!(lNode instanceof LeafNode)) {
            lNode = ((NonLeafNode) lNode).promisingChild(l);
            rNode = ((NonLeafNode) rNode).promisingChild(r);
        }
        LeafNode lCast = (LeafNode) lNode;
        LeafNode rCast = (LeafNode) rNode;

        for (; ; ) {
            ans.addAll(lCast.between(l, r));
            if (lCast == rCast) break;
            lCast = lCast.next;
        }
        double[] ansPrimitive = new double[ans.size()];
        for (int i = 0; i < ans.size(); i++) {
            ansPrimitive[i] = ans.get(i);
        }
        return ansPrimitive;
    }
}
