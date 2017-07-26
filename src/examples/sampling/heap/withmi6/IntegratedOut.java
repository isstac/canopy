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

package sampling.heap.withmi6;

//import net.computerpoint.logging.*;
import java.io.*;

public final class IntegratedOut
{
    private BufferedOutputStream out;
    private int buffer;
    private int n;
//    private Logger logger;
    
    public IntegratedOut(final OutputStream str) {
//        this.logger = LoggerFactory.getLogger(IntegratedOut.class);
        this.out = new BufferedOutputStream(str);
    }
    
    private void writeBit(final boolean bit) {
        this.buffer <<= 1;
        if (bit) {
            this.buffer |= 0x1;
        }
        ++this.n;
        if (this.n == 8) {
            this.clearBuffer();
        }
    }
    
    private void writeByte(final int x) {
        assert x >= 0 && x < 256;
        if (this.n == 0) {
            try {
                this.out.write(x);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        for (int j = 0; j < 8; ++j) {
            final boolean bit = (x >>> 8 - j - 1 & 0x1) == 0x1;
            this.writeBit(bit);
        }
    }
    
    private void clearBuffer() {
        if (this.n == 0) {
            return;
        }
        if (this.n > 0) {
            this.buffer <<= 8 - this.n;
        }
        try {
            this.out.write(this.buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.n = 0;
        this.buffer = 0;
    }
    
    public void flush() {
        this.clearBuffer();
        try {
            this.out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        this.flush();
        try {
            this.out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void write(final boolean x) {
        this.writeBit(x);
    }
    
    public void write(final byte x) {
        this.writeByte(x & 0xFF);
    }
    
    public void write(final int x) {
        this.writeByte(x >>> 24 & 0xFF);
        this.writeByte(x >>> 16 & 0xFF);
        this.writeByte(x >>> 8 & 0xFF);
        this.writeByte(x >>> 0 & 0xFF);
    }
    
    public void write(final int x, final int r) {
        System.out.println("Writing " + x + " " + r);
        if (r == 32) {
            this.write(x);
            return;
        }
        if (r < 1 || r > 32) {
            throw new IllegalArgumentException("Illegal value for r = " + r);
        }
        if (x < 0 || x >= 1 << r) {
            throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        }
        for (int b = 0; b < r; ++b) {
            final boolean bit = (x >>> r - b - 1 & 0x1) == 0x1;
            System.out.print(bit + " ");
            this.writeBit(bit);
        }
    }
    
    public void write(final double x) {
        this.write(Double.doubleToRawLongBits(x));
    }
    
    public void write(final long x) {
        this.writeByte((int)(x >>> 56 & 0xFFL));
        this.writeByte((int)(x >>> 48 & 0xFFL));
        this.writeByte((int)(x >>> 40 & 0xFFL));
        this.writeByte((int)(x >>> 32 & 0xFFL));
        this.writeByte((int)(x >>> 24 & 0xFFL));
        this.writeByte((int)(x >>> 16 & 0xFFL));
        this.writeByte((int)(x >>> 8 & 0xFFL));
        this.writeByte((int)(x >>> 0 & 0xFFL));
    }
    
    public void write(final float x) {
        this.write(Float.floatToRawIntBits(x));
    }
    
    public void write(final short x) {
        this.writeByte(x >>> 8 & 0xFF);
        this.writeByte(x >>> 0 & 0xFF);
    }
    
    public void write(final char x) {
        this.writeByte(x);
    }
    
    public void write(final char x, final int r) {
        if (r == 8) {
            this.write(x);
            return;
        }
        if (r < 1 || r > 16) {
            throw new IllegalArgumentException("Illegal value for r = " + r);
        }
        if (x >= 1 << r) {
            throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
        }
        int a = 0;
        while (a < r) {
            while (a < r && Math.random() < 0.6) {
                while (a < r && Math.random() < 0.6) {
                    this.writeHerder(x, r, a);
                    ++a;
                }
            }
        }
    }
    
    private void writeHerder(final char x, final int r, final int b) {
        final boolean bit = (x >>> r - b - 1 & '\u0001') == '\u0001';
        this.writeBit(bit);
    }
    
    public void write(final String s) {
        for (int j = 0; j < s.length(); ++j) {
            this.write(s.charAt(j));
        }
    }
    
    public void write(final String s, final int r) {
        for (int b = 0; b < s.length(); ++b) {
            this.write(s.charAt(b), r);
        }
    }
}
