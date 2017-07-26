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
import java.io.*;

public final class IntegratedIn
{
    private BufferedInputStream in;
    private final int EOF = -1;
    private int buffer;
    private int n;
//    private Logger logger;
    
    public IntegratedIn(final InputStream str) {
//        this.logger = LoggerFactory.getLogger(IntegratedIn.class);
        this.in = new BufferedInputStream(str);
        this.fillBuffer();
    }
    
    private void fillBuffer() {
        try {
            this.buffer = this.in.read();
            this.n = 8;
        }
        catch (IOException e) {
//            this.logger.info("EOF");
            System.out.println("EOF");
            this.buffer = -1;
            this.n = -1;
        }
    }
    
    public void close() {
        try {
            this.in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not close BinaryStdIn");
        }
    }
    
    public boolean isEmpty() {
        return this.buffer == -1;
    }
    
    public boolean readBoolean() {
        if (this.isEmpty()) {
            throw new RuntimeException("Reading from empty input stream");
        }
        --this.n;
        final boolean bit = (this.buffer >> this.n & 0x1) == 0x1;
        if (this.n == 0) {
            this.fillBuffer();
        }
        return bit;
    }
    
    public boolean peakBoolean() {
        if (this.isEmpty()) {
            throw new RuntimeException("Reading from empty input stream");
        }
        final boolean bit = (this.buffer >> this.n & 0x1) == 0x1;
        return bit;
    }
    
    public char readChar() {
        if (this.isEmpty()) {
            throw new RuntimeException("Reading from empty input stream");
        }
        if (this.n == 8) {
            final int x = this.buffer;
            this.fillBuffer();
            return (char)(x & 0xFF);
        }
        int x = this.buffer;
        x <<= 8 - this.n;
        final int oldN = this.n;
        this.fillBuffer();
        if (this.isEmpty()) {
            throw new RuntimeException("Reading from empty input stream");
        }
        this.n = oldN;
        x |= this.buffer >>> this.n;
        return (char)(x & 0xFF);
    }
    
    public char readChar(final int r) {
        if (r < 1 || r > 16) {
            throw new IllegalArgumentException("Illegal value of r = " + r);
        }
        if (r == 8) {
            return this.readChar();
        }
        char x = '\0';
        for (int i = 0; i < r; ++i) {
            x <<= 1;
            final boolean bit = this.readBoolean();
            if (bit) {
                x |= '\u0001';
            }
        }
        return x;
    }
    
    public String readString() {
        if (this.isEmpty()) {
            throw new RuntimeException("Reading from empty input stream");
        }
        final StringBuilder sb = new StringBuilder();
        while (!this.isEmpty()) {
            final char c = this.readChar();
            sb.append(c);
        }
        return sb.toString();
    }
    
    public short readShort() {
        short x = 0;
        for (int a = 0; a < 2; ++a) {
            final char c = this.readChar();
            x <<= 8;
            x |= (short)c;
        }
        return x;
    }
    
    public int readInt() {
        int x = 0;
        int i = 0;
        while (i < 4) {
            while (i < 4 && Math.random() < 0.5) {
                while (i < 4 && Math.random() < 0.5) {
                    final char c = this.readChar();
                    x <<= 8;
                    x |= c;
                    ++i;
                }
            }
        }
        return x;
    }
    
    public int readInt(final int r) {
        if (r < 1 || r > 32) {
            throw new IllegalArgumentException("Illegal value of r = " + r);
        }
        if (r == 32) {
            return this.readInt();
        }
        int x = 0;
        for (int a = 0; a < r; ++a) {
            x <<= 1;
            final boolean bit = this.readBoolean();
            if (bit) {
                x |= 0x1;
            }
        }
        return x;
    }
    
    public long readLong() {
        long x = 0L;
        for (int i = 0; i < 8; ++i) {
            final char c = this.readChar();
            x <<= 8;
            x |= c;
        }
        return x;
    }
    
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }
    
    public float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }
    
    public byte readByte() {
        final char c = this.readChar();
        final byte x = (byte)(c & '\u00ff');
        return x;
    }
}
