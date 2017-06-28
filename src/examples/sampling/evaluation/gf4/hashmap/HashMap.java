/*
 * Decompiled with CFR 0_114.
 */
package sampling.evaluation.gf4.hashmap;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HashMap<K, V>
extends AbstractMap<K, V> {
    transient Node<K, V>[] table;
    static final transient int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int MAXIMUM_CAPACITY = 1073741824;
    static final int MIN_TREEIFY_CAPACITY = 64;
    static final int TREEIFY_THRESHOLD = 8;
    float loadFactor = 0.75f;
    transient int capacity = 16;
    int threshold = 0;
    transient Set<Map.Entry<K, V>> entrySet;
    transient int size;

    public HashMap() {
        this(16, 0.75f);
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        this();
        this.putAll(m);
    }

    public HashMap(int capacity) {
        this(capacity, 0.75f);
    }

    public HashMap(int capacity, float loadFactor) {
        Node[] newTable;
        this.entrySet = new TreeSet<Map.Entry<K, V>>(new NodeComparator());
        this.size = 0;
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.table = newTable = new Node[capacity];
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.entrySet;
    }

    @Override
    public V put(K key, V value) {
        Object e = null;
        int h = this.hash(key);
        Node<K, V> node = this.table[h];
        if (node == null) {
            this.table[h] = node = new Node<K, V>(this.hash(key), key, value, null);
            this.entrySet.add(node);
            ++this.size;
        } else if (node instanceof TreeNode) {
            TreeNode treeNode = (TreeNode)node;
            TreeNode<K, V> result = treeNode.putTreeVal(this.table, this.hash(key), key, value);
            if (result == null) {
                this.entrySet.add(new AbstractMap.SimpleEntry<K, V>(key, value));
                ++this.size;
                return null;
            }
            if (result.value != value) {
                this.entrySet.remove(result);
                e = result.value;
                result.setValue(value);
                this.entrySet.add(result);
            }
        } else {
            int bincount = 0;
            while (node.next != null && !node.key.equals(key)) {
                node = node.next;
                ++bincount;
            }
            if (node.key.equals(key)) {
                e = node.value;
                node.value = value;
            } else {
                node.next = new Node<K, V>(this.hash(key), key, value, null);
                this.entrySet.add(node.next);
                if (bincount > 8) {
                    this.putHelper(h);
                }
                ++this.size;
            }
        }
        if ((float)this.size > (float)this.capacity * 0.75f && this.size < 1073741824) {
            this.resize();
        }
        return (V)e;
    }

    @Override
    public V get(Object key) {
        int h = this.hash((K)key);
        Node<K, V> node = this.table[h];
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            TreeNode n = (TreeNode)node;
            if ((n = n.getTreeNode(h, key)) == null) {
                return null;
            }
            return (V)n.getValue();
        }
        while (node.next != null && !node.key.equals(key)) {
            node = node.next;
        }
        if (node.key.equals(key)) {
            return node.value;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        V val = this.get(key);
        if (val == null) {
            return false;
        }
        return true;
    }

    @Override
    public V remove(Object key) {
        int h = this.hash((K)key);
        Node<K, V> node = this.table[h];
        Node<K, V> prev = null;
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            TreeNode treenode = (TreeNode)node;
            TreeNode<K, V> nodeToRemove = treenode.getTreeNode(h, key);
            if (nodeToRemove == null) {
                return null;
            }
            nodeToRemove.removeTreeNode(this.table, true);
            --this.size;
            this.entrySet.remove(new AbstractMap.SimpleEntry<Object, Object>(key, nodeToRemove.value));
            return (V)treenode.value;
        }
        while (node.next != null && !node.key.equals(key)) {
            prev = node;
            node = node.next;
        }
        if (node.key.equals(key)) {
            if (prev == null) {
                this.table[h] = node.next;
            } else {
                this.removeHelper(node, prev);
            }
            this.entrySet.remove(node);
            --this.size;
            return node.value;
        }
        return null;
    }

    private int hash(K key) {
        return Node.hash(key, this.capacity);
    }

    final Node<K, V>[] resize() {
        int newCap;
        Node<K, V>[] oldTab = this.table;
        int oldCap = oldTab == null ? 0 : oldTab.length;
        int oldThr = this.threshold;
        int newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= 1073741824) {
                this.threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            newCap = oldCap << 1;
            if (newCap < 1073741824 && oldCap >= 16) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = 16;
            newThr = 12;
        }
        if (newThr == 0) {
            float ft = (float)newCap * this.loadFactor;
            newThr = newCap < 1073741824 && ft < 1.07374182E9f ? (int)ft : Integer.MAX_VALUE;
        }
        this.threshold = newThr;
        Node[] newTab = new Node[newCap];
        this.capacity = newCap;
        this.table = newTab;
        if (oldTab != null) {
            Set<Map.Entry<K, V>> oldEntries = this.entrySet();
            this.entrySet = new TreeSet<Map.Entry<K, V>>(new NodeComparator());
            for (Map.Entry<K, V> entry : oldEntries) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
        return newTab;
    }

    private void treeify(Node<K, V>[] tab, int index) {
        int n;
        if (tab == null || (n = tab.length) < 64) {
            this.resize();
        } else {
            Node<K, V> e = tab[index];
            if (e != null) {
                TreeNode<K, V> hd = null;
                TreeNode<K, V> tl = null;
                do {
                    TreeNode<K, V> p = new TreeNode<K, V>(e.hash, e.key, e.value, null);
                    if (tl == null) {
                        hd = p;
                    } else {
                        this.treeifyHelper(tl, p);
                    }
                    tl = p;
                } while ((e = e.next) != null);
                tab[index] = hd;
                if (tab[index] != null) {
                    hd.treeify(tab);
                }
            }
        }
    }

    private void putHelper(int h) {
        this.treeify(this.table, h);
    }

    private void removeHelper(Node<K, V> node, Node<K, V> prev) {
        prev.next = node.next;
    }

    private void treeifyHelper(TreeNode<K, V> tl, TreeNode<K, V> p) {
        p.prev = tl;
        tl.next = p;
    }

    class NodeComparator
    implements Comparator {
        NodeComparator() {
        }

        public int compare(Object a, Object b) {
            if (a instanceof Map.Entry && b instanceof Map.Entry) {
                Map.Entry ae = (Map.Entry)a;
                Map.Entry be = (Map.Entry)b;
                Object ak = ae.getKey();
                Object av = ae.getValue();
                Object bk = be.getKey();
                Object bv = be.getValue();
                if (ak.equals(bk) && (av == null && bv == null || av.equals(bv))) {
                    return 0;
                }
                int avHash = 0;
                int bvHash = 0;
                if (av != null) {
                    avHash = av.hashCode();
                }
                if (bv != null) {
                    bvHash = bv.hashCode();
                }
                if (ak.hashCode() < bk.hashCode() || ak.hashCode() == bk.hashCode() && avHash < bvHash) {
                    return -1;
                }
                return 1;
            }
            return Integer.compare(a.hashCode(), b.hashCode());
        }
    }

}

