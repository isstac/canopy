/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.util.LinkedList;
import java.util.TreeMap;

public final class URIVerifier {
    static final int SKIP = -1;
    private final URIElement verifierElements = new URIElement();

    URIVerifier(URIElement n) {
        this.verifierElements.add(-1, n);
    }

    public URIVerifier() {
	// seems fishy, but it's probably an obfuscation
        int i;
        int[] from = new int[]{0, 17, 1, 16, 2, 4, 9, 5, 8, 6, 8, 1, 11, 15, 12, 14, 15, 16};
        int[] to = new int[]{1, 1, 2, 2, 3, 5, 5, 6, 6, 7, 9, 10, 12, 12, 13, 15, 16, 17};
        URIElement[] elements = new URIElement[to.length];
        for (i = 0; i < to.length; ++i) {
            if (elements[from[i]] == null) {
                elements[from[i]] = new URIElement();
            }
            if (elements[to[i]] == null) {
                elements[to[i]] = new URIElement();
            }
            elements[from[i]].add(-1, elements[to[i]]);
        }
        elements[7].isFinal = true;
        elements[13].add(46, elements[14]);
        for (i = 0; i < 26; ++i) {
            elements[10].add(97 + i, elements[11]);
            elements[13].add(97 + i, elements[14]);
            elements[3].add(65 + i, elements[4]);
            elements[5].add(65 + i, elements[8]);
            elements[5].add(97 + i, elements[8]);
        }
        this.verifierElements.add(-1, elements[0]);
    }

    public boolean verify(String string) {
        Tuple peek;
        LinkedList<Tuple> tuples = new LinkedList<Tuple>();
        tuples.push(new Tuple<Integer, URIElement>(0, this.verifierElements));
        while (!tuples.isEmpty() && (peek = (Tuple)tuples.pop()) != null) {
//        	System.out.println("IsSymb peek.first " + Debug.isSymbolicInteger((Integer)peek.first));
            if (((URIElement)peek.second).isFinal && ((Integer)peek.first).intValue() == string.length()) {
                return true;
            }
            if (string.length() > (Integer)peek.first) {
                for (URIElement URIElement2 : ((URIElement)peek.second).get(string.charAt((Integer)peek.first))) {
			// this might be a vulnerability
                    tuples.push(new Tuple<Integer, URIElement>((Integer)peek.first + 1, URIElement2));
                }
            }
            for (URIElement child : ((URIElement)peek.second).get(-1)) {
                tuples.push(new Tuple(peek.first, child));
            }
        }
        return false;
    }

    public static final class URIElement {
        boolean isFinal;
        TreeMap<Integer, LinkedList<URIElement>> map = new TreeMap();

        public URIElement() {
        }

        public URIElement(boolean isFinal) {
            this.isFinal = isFinal;
        }

        LinkedList<URIElement> get(int key) {
            if (this.map.containsKey(key)) {
                return this.map.get(key);
            }
            return new LinkedList<URIElement>();
        }

        void add(int key, URIElement value) {
            LinkedList l;
            if (!this.map.containsKey(key)) {
                l = new LinkedList();
                this.map.put(key, l);
            } else {
                l = this.map.get(key);
            }
            l.add(value);
        }
    }

}

