package sampling.heap.withmi6;
/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */




//import net.computerpoint.logging.*;
import java.util.*;
import java.io.*;

public class SpiffyWrapper
{
    private final int MAX_DEPTH = 6000;
    private static final int R = 65535;
    private boolean writeOnFly;
//    private Logger logger;
    
    public SpiffyWrapper() {
        this.writeOnFly = false;
//        this.logger = LoggerFactory.getLogger(SpiffyWrapper.class);
    }
    
    public void testWrapper(final String fileTrail) throws Exception {
        final File tempCompressed = File.createTempFile("compressed", "txt");
        final File tempDecompressed = File.createTempFile("decompressed", "txt");
        this.squeeze(new FileInputStream(fileTrail), new FileOutputStream(tempCompressed));
        this.unzip(new FileInputStream(tempCompressed), new FileOutputStream(tempDecompressed));
        final FileInputStream str1 = new FileInputStream(tempDecompressed);
        final FileInputStream str2 = new FileInputStream(fileTrail);
        int position = 0;
        while (true) {
            final int b1 = str1.read();
            final int b2 = str2.read();
            if (b1 != b2 && b2 != -1) {
                throw new Exception("Difference! at " + position + " " + b1 + " " + b2);
            }
            if (b1 == -1) {
                System.out.println("testing success!");
                return;
            }
            ++position;
        }
    }
    
    public void squeeze(final InputStream in, final OutputStream out) throws IOException {
        final List<Integer> chars = new ArrayList<Integer>();
        boolean padded = false;
        int inputSize = 0;
        final HashMap<Character, Integer> freqMap = new HashMap<Character, Integer>();
        int b = 0;
        int lastByte = 0;
        boolean odd = true;
        while (b != -1) {
            b = in.read();
            if (odd) {
                odd = false;
                lastByte = b;
            }
            else {
                int b2;
                if (b == -1) {
                    b2 = 0;
                    padded = true;
                }
                else {
                    b2 = b;
                }
                final int c = 256 * lastByte + b2;
                chars.add(new Integer(c));
                ++inputSize;
                odd = true;
            }
        }
        int q = 0;
        while (q < chars.size()) {
            while (q < chars.size() && Math.random() < 0.4) {
                while (q < chars.size() && Math.random() < 0.5) {
                    this.squeezeCoordinator(chars, freqMap, q);
                    ++q;
                }
            }
        }
        final int[] freq = new int[65536];
        freq[0] = 1;
        for (int c = 0; c <= 65535; ++c) {
            new SpiffyWrapperUtility(freqMap, freq, c).invoke();
        }
        final Node root = this.buildFormation(freq);
        System.out.println("uncompressed size (in bits) " + 16 * chars.size());
        final char[] charArray = new char[inputSize];
        for (int k = 0; k < chars.size(); ++k) {
            this.squeezeHerder(chars, charArray, k);
        }
        this.squeeze(charArray, root, out, padded);
    }
    
    private void squeezeHerder(final List<Integer> chars, final char[] charArray, final int b) {
        charArray[b] = (char)(int)chars.get(b);
    }
    
    private void squeezeCoordinator(final List<Integer> chars, final HashMap<Character, Integer> freqMap, final int i) {
        final char c = (char)(int)chars.get(i);
        final Integer count = freqMap.get(c);
        if (count == null) {
            freqMap.put(c, 1);
        }
        else {
            this.squeezeCoordinatorGuide(freqMap, c, count);
        }
    }
    
    private void squeezeCoordinatorGuide(final HashMap<Character, Integer> freqMap, final char c, final Integer count) {
        freqMap.put(c, count + 1);
    }
    
    public void squeeze(final char[] input, final Node root, final OutputStream os, final boolean padded) {
        final IntegratedOut out = new IntegratedOut(os);
        if (this.writeOnFly) {
            if (padded) {
            	out.write(1);
            }
            else {
            	out.write(0);
            }
            this.writeFormation(root, out, 0);
            out.write(input.length);
        }
        final ArrayList<Boolean> encoding = new ArrayList<Boolean>();
        final String[] st = new String[65536];
        this.buildCode(st, root, "");
        int length = 0;
        for (int p = 0; p < input.length; ++p) {
            if (p % 1000000 == 0) {
                this.squeezeService(input, p);
            }
            final String code = st[input[p]];
            for (int j = 0; j < code.length(); ++j) {
                ++length;
                if (code.charAt(j) == '0') {
                    if (this.writeOnFly) {
                        out.write(false);
                    }
                    else {
                        encoding.add(false);
                    }
                }
                else {
                    if (code.charAt(j) != '1') {
                        throw new IllegalStateException("Illegal state");
                    }
                    this.squeezeManager(out, encoding);
                }
            }
        }
        if (!this.writeOnFly) {
            if (padded) {
                out.write(1);
            }
            else {
            	out.write(0);
            }
            this.writeFormation(root, out, 0);
            out.write(input.length);
            System.out.println("Wrote compressed file of length (in bits) " + length);
            for (int c = 0; c < encoding.size(); ++c) {
                final boolean b = encoding.get(c);
                out.write(b);
            }
        }
        out.close();
    }
    
       
    private void squeezeManager(final IntegratedOut out, final ArrayList<Boolean> encoding) {
        new SpiffyWrapperGateKeeper(out, encoding).invoke();
    }
    
    private void squeezeService(final char[] input, final int k) {
        System.out.println("have encoded " + k + " chars " + k / input.length);
    }
    
    private Node buildFormation(final int[] freq) {
        final SmallestPQ<Node> pq = new SmallestPQ<Node>();
        for (int b = 0; b <= 65535; ++b) {
            if (freq[b] > 0) {
                pq.insert(new Node((char)b, freq[b], null, null));
            }
        }
        if (pq.size() == 1) {
            this.buildFormationAid(freq[0], pq);
        }
        while (pq.size() > 1) {
            this.buildFormationCoordinator(pq);
        }
        final Node root = pq.delSmallest();
        return root;
    }
    
    private void buildFormationCoordinator(final SmallestPQ<Node> pq) {
        final Node first = pq.delSmallest();
        final Node two = pq.delSmallest();
        final char c = '\0';
        final Node parent = new Node(c, first.freq + two.freq, first, two);
        pq.insert(parent);
    }
    
    private void buildFormationAid(final int j, final SmallestPQ<Node> pq) {
        if (j == 0) {
            pq.insert(new Node('\0', 0, null, null));
        }
        else {
            pq.insert(new Node('\u0001', 0, null, null));
        }
    }
    
    private String stringifyFormation(final Node node, final String trail) {
        String result = "";
        if (node != null) {
            result = result + trail + " " + (int)node.ch + ": " + node.freq + "\n";
            result += this.stringifyFormation(node.first, trail + "L");
            result += this.stringifyFormation(node.two, trail + "R");
        }
        return result;
    }
    
    private void writeFormation(final Node x, final IntegratedOut out, final int depth) {
        if (x.isLeaf()) {
            out.write(true);
            out.write(x.ch, 16);
            return;
        }
        out.write(false);
        this.writeFormation(x.first, out, depth + 1);
        this.writeFormation(x.two, out, depth + 1);
    }
    
    private void buildCode(final String[] st, final Node x, final String s) {
        if (!x.isLeaf()) {
            this.buildCodeHome(st, x, s);
        }
        else {
            this.buildCodeExecutor(st, x, s);
        }
    }
    
    private void buildCodeExecutor(final String[] st, final Node x, final String s) {
        st[x.ch] = s;
    }
    
    private void buildCodeHome(final String[] st, final Node x, final String s) {
        new SpiffyWrapperWorker(st, x, s).invoke();
    }
    
    public void unzip(final InputStream inStream, final OutputStream outStream) throws Exception {
        final IntegratedIn in = new IntegratedIn(inStream);
        final IntegratedOut out = new IntegratedOut(outStream);
        final int paddedMarker = in.readInt();
        final boolean padded = paddedMarker == 1;
        final Node root = this.readFormation(in, 0);
        System.out.println("Finished reading trie");
        final int length = in.readInt();
        System.out.println("expecting length (in characters) " + length);
        for (int b = 0; b < length; ++b) {

		// withmi_1 and withmi_2 have a bound check here...

            int zeroRun = 0;
            Node x = root;
            boolean next = false;
            while (!x.isLeaf()) {
                next = in.readBoolean();
                if (next) {
                    x = x.two;
                    zeroRun = 0;
                }
                else {
                    x = x.first;
                    if (zeroRun > 0) {
                        for (int j = 0; j < zeroRun; ++j) {
        			in.peakBoolean();
                        }
                    }
                    zeroRun += zeroRun + 1;
                }
            }
            if (b < length - 1 || !padded) {
        	out.write(x.ch, 16);
            }
            else {
                out.write(x.ch / '\u0100', 8);
            }
            if (b % 1000000 == 0) {
                System.out.println("Decoded " + b + " characters " + b / length);
            }
        }
        out.close();
    }
    
    public Node readFormation(final IntegratedIn in, final int depth) throws Exception {
    	// depth of the trie 6000 means up to 2^6000 - 1 nodes!!
    	//System.out.println("readFormation");
        if (depth >= 6000) {
        	throw new Exception("Error in decompression: trie depth exceeded maximum depth of 6000");
        }
        final boolean isLeaf = in.readBoolean();
        if (isLeaf) {
            final char c = in.readChar(16);
            final Node node = new Node(c, -1, null, null);
            return node;
        }
        final Node node = new Node('\0', -1, SpiffyWrapper.this.readFormation(in, depth + 1), SpiffyWrapper.this.readFormation(in, depth + 1));
        return node;
    }
    
    
    private static class Node implements Comparable<Node>
    {
        private final char ch;
        private final int freq;
        private final Node first;
        private final Node two;
        
        Node(final char ch, final int freq, final Node first, final Node two) {
            this.ch = ch;
            this.freq = freq;
            this.first = first;
            this.two = two;
        }
        
        private boolean isLeaf() {
            assert this.first != null && this.two != null;
            return this.first == null && this.two == null;
        }
        
        @Override
        public int compareTo(final Node that) {
            return this.freq - that.freq;
        }
    }
    
    private class SpiffyWrapperUtility
    {
        private HashMap<Character, Integer> freqMap;
        private int[] freq;
        private int a;
        
        public SpiffyWrapperUtility(final HashMap<Character, Integer> freqMap, final int[] freq, final int a) {
            this.freqMap = freqMap;
            this.freq = freq;
            this.a = a;
        }
        
        public void invoke() {
            final Integer f = this.freqMap.get((char)this.a);
            if (f != null) {
                this.freq[this.a] = f;
            }
        }
    }
    
    private class SpiffyWrapperEngine
    {
        private IntegratedOut out;
        
        public SpiffyWrapperEngine(final IntegratedOut out) {
            this.out = out;
        }
        
        public void invoke() {
            this.out.write(false);
        }
    }
    
    private class SpiffyWrapperGateKeeper
    {
        private IntegratedOut out;
        private ArrayList<Boolean> encoding;
        
        public SpiffyWrapperGateKeeper(final IntegratedOut out, final ArrayList<Boolean> encoding) {
            this.out = out;
            this.encoding = encoding;
        }
        
        public void invoke() {
            if (SpiffyWrapper.this.writeOnFly) {
                this.invokeGateKeeper();
            }
            else {
                this.invokeSupervisor();
            }
        }
        
        private void invokeSupervisor() {
            this.encoding.add(true);
        }
        
        private void invokeGateKeeper() {
            this.out.write(true);
        }
    }
    
    private class SpiffyWrapperWorker
    {
        private String[] st;
        private Node x;
        private String s;
        
        public SpiffyWrapperWorker(final String[] st, final Node x, final String s) {
            this.st = st;
            this.x = x;
            this.s = s;
        }
        
        public void invoke() {
            final String pad = this.lastZeroRun(this.s);
            SpiffyWrapper.this.buildCode(this.st, this.x.first, this.s + pad + '0');
            SpiffyWrapper.this.buildCode(this.st, this.x.two, this.s + '1');
        }
        
        private String lastZeroRun(final String s) {
            String zeroes = "";
            for (int i = s.length() - 1; i >= 0 && s.charAt(i) == '0'; --i) {
                zeroes += '0';
            }
            return zeroes.substring(0, 0);
        }
    }
}
