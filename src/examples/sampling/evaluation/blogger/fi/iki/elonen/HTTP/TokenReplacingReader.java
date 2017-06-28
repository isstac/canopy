/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.CharBuffer;

public class TokenReplacingReader
extends Reader {
    protected PushbackReader pushbackReader = null;
    protected ITokenResolver tokenResolver = null;
    protected StringBuilder tokenNameBuffer = new StringBuilder();
    protected String tokenValue = null;
    protected int tokenValueIndex = 0;

    public TokenReplacingReader(Reader source, ITokenResolver resolver) {
        this.pushbackReader = new PushbackReader(source, 2);
        this.tokenResolver = resolver;
    }

    public TokenReplacingReader(InputStream source, ITokenResolver resolver) {
        this.pushbackReader = new PushbackReader(new InputStreamReader(source), 2);
        this.tokenResolver = resolver;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = this.read(cbuf, 0, len);
        if (n > 0) {
            target.put(cbuf, 0, n);
        }
        return n;
    }

    @Override
    public int read() throws IOException {
        int data;
        if (this.tokenValue != null) {
            if (this.tokenValueIndex < this.tokenValue.length()) {
                return this.tokenValue.charAt(this.tokenValueIndex++);
            }
            if (this.tokenValueIndex == this.tokenValue.length()) {
                this.tokenValue = null;
                this.tokenValueIndex = 0;
            }
        }
        if ((data = this.pushbackReader.read()) != 36) {
            return data;
        }
        data = this.pushbackReader.read();
        if (data != 123) {
            this.pushbackReader.unread(data);
            return 36;
        }
        this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());
        data = this.pushbackReader.read();
        while (data != 125) {
            this.tokenNameBuffer.append((char)data);
            data = this.pushbackReader.read();
        }
        this.tokenValue = this.tokenResolver.resolveToken(this.tokenNameBuffer.toString());
        if (this.tokenValue == null) {
            this.tokenValue = "${" + this.tokenNameBuffer.toString() + "}";
        }
        if (this.tokenValue.length() == 0) {
            return this.read();
        }
        return this.tokenValue.charAt(this.tokenValueIndex++);
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return this.read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = 0;
        for (int i = 0; i < len; ++i) {
            int nextChar = this.read();
            if (nextChar == -1) {
                if (charsRead != 0) break;
                charsRead = -1;
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char)nextChar;
        }
        return charsRead;
    }

    @Override
    public void close() throws IOException {
        this.pushbackReader.close();
    }

    @Override
    public long skip(long n) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public boolean ready() throws IOException {
        return this.pushbackReader.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public void reset() throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    public String toString() {
        StringWriter stringWriter = new StringWriter();
        try {
            int c;
            while ((c = this.read()) != -1) {
                stringWriter.write(c);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Problem writing file to string buffer");
        }
        return stringWriter.toString();
    }
}

