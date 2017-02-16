/*
 * Decompiled with CFR 0_114.
 */
package sampling.engagement1.gf4.hashmap;

import sampling.engagement1.gf4.hashmap.HashMap;
import sampling.engagement1.gf4.hashmap.Node;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;

final class TreeNode<K, V>
extends Node<K, V> {
    final int UNTREEIFY_THRESHOLD = 6;
    TreeNode<K, V> parent;
    TreeNode<K, V> left;
    TreeNode<K, V> right;
    TreeNode<K, V> prev;
    boolean red;

    TreeNode(int hash, K key, V val, Node<K, V> next) {
        super(hash, key, val, next);
    }

    final TreeNode<K, V> root() {
        TreeNode<K, V> r = this;
        TreeNode<K, V> p;
        while ((p = r.parent) != null) {
            r = p;
        }
        return r;
    }

    static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
        int n;
        if (root != null && tab != null && (n = tab.length) > 0) {
            TreeNode.moveRootToFrontHelper(root, n, tab);
        }
    }

    final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
        TreeNode<K, V> p = this;
        do {
            int dir = 0;
            TreeNode<K, V> pl = p.left;
            TreeNode<K, V> pr = p.right;
            int ph = p.hash;
            if (ph > h) {
                p = pl;
                continue;
            }
            if (ph < h) {
                p = pr;
                continue;
            }
            Object pk = p.key;
            if (pk == k || k != null && k.equals(pk)) {
                return p;
            }
            if (pl == null) {
                p = pr;
                continue;
            }
            if (pr == null) {
                p = pl;
                continue;
            }
            if ((kc != null || (dir = this.compareComparables(kc, k, pk)) != 0)) {
                p = dir < 0 ? pl : pr;
                continue;
            }
            TreeNode<K, V> q = pr.find(h, k, kc);
            if (q != null) {
                return q;
            }
            p = pl;
        } while (p != null);
        return null;
    }

    final TreeNode<K, V> getTreeNode(int h, Object k) {
        return (this.parent != null ? this.root() : this).find(h, k, null);
    }

    static int tieBreakOrder(Object a, Object b) {
        int d;
        if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0) {
            d = System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
        }
        return d;
    }

    final void treeify(Node<K, V>[] tab) {
        TreeNode<K, V> root = null;
        TreeNode<K, V> x = this;
        while (x != null) {
            TreeNode next = (TreeNode)x.next;
            x.right = null;
            x.left = null;
            if (root == null) {
                x.parent = null;
                x.red = false;
                root = x;
            } else {
                int dir = 0;
                TreeNode<K, V> xp;
                Object k = x.key;
                int h = x.hash;
                Class kc = null;
                TreeNode<K, V> p = root;
                do {
                    Object pk = p.key;
                    int ph = p.hash;
                    if (ph > h) {
                        dir = -1;
                    } else if (ph < h) {
                        dir = 1;
                    } else if (kc == null && (dir = this.compareComparables(kc, k, pk)) == 0) {
                        dir = TreeNode.tieBreakOrder(k, pk);
                    }
                    xp = p;
                } while ((p = dir <= 0 ? p.left : p.right) != null);
                x.parent = xp;
                if (dir <= 0) {
                    xp.left = x;
                } else {
                    xp.right = x;
                }
                root = TreeNode.balanceInsertion(root, x);
            }
            x = next;
        }
        TreeNode.moveRootToFront(tab, root);
    }

    final Node<K, V> untreeify() {
        Node hd = null;
        Node tl = null;
        Node q = this;
        while (q != null) {
            Random randomNumberGeneratorInstance = new Random();
            while (q != null && randomNumberGeneratorInstance.nextDouble() < 0.5) {
                Node p = new Node(q.hash, q.key, q.value, null);
                if (tl == null) {
                    hd = p;
                } else {
                    tl.next = p;
                }
                tl = p;
                q = q.next;
            }
        }
        return hd;
    }

    final TreeNode<K, V> putTreeVal(Node<K, V>[] tab, int h, K k, V v) {
        int dir = 0;
        TreeNode<K, V> root;
        TreeNode<K, V> xp;
        Class kc = null;
        boolean searched = false;
        TreeNode<K, V> p = root = this.parent != null ? this.root() : this;
        do {
            int ph;
            if ((ph = p.hash) > h) {
                dir = -1;
            } else if (ph < h) {
                dir = 1;
            } else {
                Object pk = p.key;
                if (pk == k || pk != null && k.equals(pk)) {
                    return p;
                }
                if (kc == null && (dir = this.compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K, V> q;
                        searched = true;
                        TreeNode<K, V> ch = p.left;
                        if (ch != null && (q = ch.find(h, k, kc)) != null || (ch = p.right) != null && (q = ch.find(h, k, kc)) != null) {
                            return q;
                        }
                    }
                    dir = TreeNode.tieBreakOrder(k, pk);
                }
            }
            xp = p;
        } while ((p = dir <= 0 ? p.left : p.right) != null);
        Node xpn = xp.next;
        TreeNode<K, V> x = new TreeNode<K, V>(h, k, v, xpn);
        if (dir <= 0) {
            xp.left = x;
        } else {
            xp.right = x;
        }
        xp.next = x;
        x.parent = x.prev = xp;
        if (xpn != null) {
            ((TreeNode)xpn).prev = x;
        }
        return null;
    }

    final void removeTreeNode(Node<K, V>[] tab, boolean movable) {
        TreeNode<K, V> replacement;
        TreeNode<K, V> r;
        TreeNode<K, V> rl;
        TreeNode<K, V> first;
        int n;
        if (tab == null || (n = tab.length) == 0) {
            return;
        }
        int index = n - 1 & this.hash;
        TreeNode<K, V> root = first = (TreeNode<K, V>)tab[index];
        TreeNode<K, V> succ = (TreeNode<K, V>)this.next;
        TreeNode<K, V> pred = this.prev;
        if (pred == null) {
            tab[index] = first = succ;
        } else {
            pred.next = succ;
        }
        if (succ != null) {
            succ.prev = pred;
        }
        if (first == null) {
            return;
        }
        if (root.parent != null) {
            root = root.root();
        }
        if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) {
            tab[index] = first.untreeify();
            return;
        }
        TreeNode<K, V> p = this;
        TreeNode<K, V> pl = this.left;
        TreeNode<K, V> pr = this.right;
        if (pl != null && pr != null) {
            TreeNode<K, V> sl;
            TreeNode<K, V> s = pr;
            while ((sl = s.left) != null) {
                s = sl;
            }
            boolean c = s.red;
            s.red = p.red;
            p.red = c;
            TreeNode<K, V> sr = s.right;
            TreeNode<K, V> pp = p.parent;
            if (s == pr) {
                this.removeTreeNodeHelper(s, p);
            } else {
                this.removeTreeNodeHelper1(s, p, pr);
            }
            p.left = null;
            p.right = sr;
            if (p.right != null) {
                sr.parent = p;
            }
            if ((s.left = pl) != null) {
                pl.parent = s;
            }
            if ((s.parent = pp) == null) {
                root = s;
            } else if (p == pp.left) {
                pp.left = s;
            } else {
                pp.right = s;
            }
            replacement = sr != null ? sr : p;
        } else {
            replacement = pl != null ? pl : (pr != null ? pr : p);
        }
        if (replacement != p) {
            replacement.parent = p.parent;
            TreeNode<K, V> pp = replacement.parent;
            if (pp == null) {
                root = replacement;
            } else if (p == pp.left) {
                pp.left = replacement;
            } else {
                pp.right = replacement;
            }
            p.parent = null;
            p.right = null;
            p.left = null;
        }
        TreeNode<K, V> treeNode = r = p.red ? root : TreeNode.balanceDeletion(root, replacement);
        if (replacement == p) {
            this.removeTreeNodeHelper2(p);
        }
        if (movable) {
            TreeNode.moveRootToFront(tab, r);
        }
    }

    final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
        this.splitHelper(bit, index, tab);
    }

    static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
        TreeNode<K, V> r;
        if (p != null && (r = p.right) != null) {
            TreeNode<K, V> rl = p.right = r.left;
            if (p.right != null) {
                rl.parent = p;
            }
            TreeNode<K, V> pp = r.parent = p.parent;
            if (r.parent == null) {
                root = r;
                r.red = false;
            } else if (pp.left == p) {
                pp.left = r;
            } else {
                pp.right = r;
            }
            r.left = p;
            p.parent = r;
        }
        return root;
    }

    static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
        TreeNode<K, V> l;
        if (p != null && (l = p.left) != null) {
            TreeNode<K, V> lr = p.left = l.right;
            if (p.left != null) {
                lr.parent = p;
            }
            TreeNode<K, V> pp = l.parent = p.parent;
            if (l.parent == null) {
                root = l;
                l.red = false;
            } else if (pp.right == p) {
                pp.right = l;
            } else {
                pp.left = l;
            }
            l.right = p;
            p.parent = l;
        }
        return root;
    }

    static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
        x.red = true;
        do {
            TreeNode<K, V> xpp;
            TreeNode<K, V> xp;
            if ((xp = x.parent) == null) {
                x.red = false;
                return x;
            }
            if (!xp.red || (xpp = xp.parent) == null) {
                return root;
            }
            TreeNode<K, V> xppl = xpp.left;
            if (xp == xppl) {
                TreeNode<K, V> xppr = xpp.right;
                if (xppr != null && xppr.red) {
                    xppr.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                    continue;
                }
                if (x == xp.right) {
                    x = xp;
                    root = TreeNode.rotateLeft(root, x);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpp = xp == null ? null : xp.parent;
                }
                if (xp == null) continue;
                xp.red = false;
                if (xpp == null) continue;
                xpp.red = true;
                root = TreeNode.rotateRight(root, xpp);
                continue;
            }
            if (xppl != null && xppl.red) {
                xppl.red = false;
                xp.red = false;
                xpp.red = true;
                x = xpp;
                continue;
            }
            if (x == xp.left) {
                x = xp;
                root = TreeNode.rotateRight(root, x);
                xp = x.parent;
                TreeNode<K, V> treeNode = xpp = xp == null ? null : xp.parent;
            }
            if (xp == null) continue;
            xp.red = false;
            if (xpp == null) continue;
            xpp.red = true;
            root = TreeNode.rotateLeft(root, xpp);
        } while (true);
    }

    static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
        while (x != null && x != root) {
            TreeNode<K, V> sl;
            TreeNode<K, V> sr;
            TreeNode<K, V> xp = x.parent;
            if (xp == null) {
                x.red = false;
                return x;
            }
            if (x.red) {
                x.red = false;
                return root;
            }
            TreeNode<K, V> xpl = xp.left;
            if (xpl == x) {
                TreeNode<K, V> xpr = xp.right;
                if (xpr != null && xpr.red) {
                    xpr.red = false;
                    xp.red = true;
                    root = TreeNode.rotateLeft(root, xp);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpr = xp == null ? null : xp.right;
                }
                if (xpr == null) {
                    x = xp;
                    continue;
                }
                sl = xpr.left;
                sr = xpr.right;
                if (!(sr != null && sr.red || sl != null && sl.red)) {
                    xpr.red = true;
                    x = xp;
                    continue;
                }
                if (sr == null || !sr.red) {
                    if (sl != null) {
                        sl.red = false;
                    }
                    xpr.red = true;
                    root = TreeNode.rotateRight(root, xpr);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpr = xp == null ? null : xp.right;
                }
                if (xpr != null) {
                    xpr.red = xp == null ? false : xp.red;
                    sr = xpr.right;
                    if (sr != null) {
                        sr.red = false;
                    }
                }
                if (xp != null) {
                    xp.red = false;
                    root = TreeNode.rotateLeft(root, xp);
                }
                x = root;
                continue;
            }
            if (xpl != null && xpl.red) {
                xpl.red = false;
                xp.red = true;
                root = TreeNode.rotateRight(root, xp);
                xp = x.parent;
                TreeNode<K, V> treeNode = xpl = xp == null ? null : xp.left;
            }
            if (xpl == null) {
                x = xp;
                continue;
            }
            sl = xpl.left;
            sr = xpl.right;
            if (!(sl != null && sl.red || sr != null && sr.red)) {
                xpl.red = true;
                x = xp;
                continue;
            }
            if (sl == null || !sl.red) {
                if (sr != null) {
                    sr.red = false;
                }
                xpl.red = true;
                root = TreeNode.rotateLeft(root, xpl);
                xp = x.parent;
                TreeNode<K, V> treeNode = xpl = xp == null ? null : xp.left;
            }
            if (xpl != null) {
                xpl.red = xp == null ? false : xp.red;
                sl = xpl.left;
                if (sl != null) {
                    sl.red = false;
                }
            }
            if (xp != null) {
                xp.red = false;
                root = TreeNode.rotateRight(root, xp);
            }
            x = root;
        }
        return root;
    }

    static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
        TreeNode<K, V> tp = t.parent;
        TreeNode<K, V> tl = t.left;
        TreeNode<K, V> tr = t.right;
        TreeNode<K, V> tb = t.prev;
        TreeNode tn = (TreeNode)t.next;
        if (tb != null && tb.next != t) {
            return false;
        }
        if (tn != null && tn.prev != t) {
            return false;
        }
        if (tp != null && t != tp.left && t != tp.right) {
            return false;
        }
        if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
            return false;
        }
        if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
            return false;
        }
        if (t.red && tl != null && tl.red && tr != null && tr.red) {
            return false;
        }
        if (tl != null && !TreeNode.checkInvariants(tl)) {
            return false;
        }
        if (tr != null && !TreeNode.checkInvariants(tr)) {
            return false;
        }
        return true;
    }

    Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class c = x.getClass();
            if (c == String.class) {
                return c;
            }
            Type[] ts = c.getGenericInterfaces();
            if (ts != null) {
                int i = 0;
                while (i < ts.length) {
                    Random randomNumberGeneratorInstance = new Random();
                    while (i < ts.length && randomNumberGeneratorInstance.nextDouble() < 0.5) {
                        Type[] as;
                        ParameterizedType p;
                        Type t = ts[i];
                        if (t instanceof ParameterizedType && (p = (ParameterizedType)t).getRawType() == Comparable.class && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c) {
                            return c;
                        }
                        ++i;
                    }
                }
            }
        }
        return null;
    }

    int compareComparables(Class<?> kc, Object k, Object x) {
        return x == null || x.getClass() != kc ? 0 : ((Comparable)k).compareTo(x);
    }

    private static <K, V> void moveRootToFrontHelper(TreeNode<K, V> root, int n, Node<K, V>[] tab) {
        int index = n - 1 & root.hash;
        TreeNode first = (TreeNode)tab[index];
        if (root != first) {
            tab[index] = root;
            TreeNode<K, V> rp = root.prev;
            Node rn = root.next;
            if (rn != null) {
                ((TreeNode)rn).prev = rp;
            }
            if (rp != null) {
                rp.next = rn;
            }
            if (first != null) {
                first.prev = root;
            }
            root.next = first;
            root.prev = null;
        }
        assert (TreeNode.checkInvariants(root));
    }

    private final void removeTreeNodeHelper(TreeNode<K, V> s, TreeNode<K, V> p) {
        p.parent = s;
        s.right = p;
    }

    private final void removeTreeNodeHelper1(TreeNode<K, V> s, TreeNode<K, V> p, TreeNode<K, V> pr) {
        TreeNode<K, V> sp = s.parent;
        p.parent = sp;
        if (p.parent != null) {
            if (s == sp.left) {
                sp.left = p;
            } else {
                sp.right = p;
            }
        }
        if ((s.right = pr) != null) {
            pr.parent = s;
        }
    }

    private final void removeTreeNodeHelper2(TreeNode<K, V> p) {
        TreeNode<K, V> pp = p.parent;
        p.parent = null;
        if (pp != null) {
            if (p == pp.left) {
                pp.left = null;
            } else if (p == pp.right) {
                pp.right = null;
            }
        }
    }

    private final void splitHelper(int bit, int index, Node<K, V>[] tab) {
        TreeNode b = this;
        TreeNode loHead = null;
        TreeNode loTail = null;
        TreeNode hiHead = null;
        TreeNode hiTail = null;
        int lc = 0;
        int hc = 0;
        TreeNode e = b;
        while (e != null) {
            Random randomNumberGeneratorInstance = new Random();
            while (e != null && randomNumberGeneratorInstance.nextDouble() < 0.5) {
                TreeNode next = (TreeNode)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    e.prev = loTail;
                    if (e.prev == null) {
                        loHead = e;
                    } else {
                        loTail.next = e;
                    }
                    loTail = e;
                    ++lc;
                } else {
                    e.prev = hiTail;
                    if (e.prev == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                    ++hc;
                }
                e = next;
            }
        }
        if (loHead != null) {
            if (lc <= 6) {
                tab[index] = loHead.untreeify();
            } else {
                tab[index] = loHead;
                if (hiHead != null) {
                    loHead.treeify(tab);
                }
            }
        }
        if (hiHead != null) {
            if (hc <= 6) {
                tab[index + bit] = hiHead.untreeify();
            } else {
                tab[index + bit] = hiHead;
                if (loHead != null) {
                    hiHead.treeify(tab);
                }
            }
        }
    }
}

