/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public abstract class NanoHTTPD {
    private static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
    private static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile("([ |\t]*Content-Disposition[ |\t]*:)(.*)", 2);
    private static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("([ |\t]*content-type[ |\t]*:)(.*)", 2);
    private static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
    private static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile("[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]");
    public static final int SOCKET_READ_TIMEOUT = 5000;
    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_HTML = "text/html";
    private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
    private static final Logger LOG = Logger.getLogger(NanoHTTPD.class.getName());
    private final String hostname;
    private final int myPort;
    private ServerSocket myServerSocket;
    private SSLServerSocketFactory sslServerSocketFactory;
    private Thread myThread;
    protected AsyncRunner asyncRunner;
    private TempFileManagerFactory tempFileManagerFactory;

    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManager[] keyManagers) throws IOException {
        SSLServerSocketFactory res = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(loadedKeyStore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
            res = ctx.getServerSocketFactory();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return res;
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManagerFactory loadedKeyFactory) throws IOException {
        SSLServerSocketFactory res = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(loadedKeyStore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(loadedKeyFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            res = ctx.getServerSocketFactory();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return res;
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(String keyAndTrustStoreClasspathPath, char[] passphrase) throws IOException {
        SSLServerSocketFactory res = null;
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = NanoHTTPD.class.getResourceAsStream(keyAndTrustStoreClasspathPath);
            keystore.load(keystoreStream, passphrase);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            res = ctx.getServerSocketFactory();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return res;
    }

    private static final void safeClose(Object closeable) {
        block5 : {
            try {
                if (closeable == null) break block5;
                if (closeable instanceof Closeable) {
                    ((Closeable)closeable).close();
                    break block5;
                }
                if (closeable instanceof Socket) {
                    ((Socket)closeable).close();
                    break block5;
                }
                if (closeable instanceof ServerSocket) {
                    ((ServerSocket)closeable).close();
                    break block5;
                }
                throw new IllegalArgumentException("Unknown object to close");
            }
            catch (IOException e) {
                LOG.log(Level.SEVERE, "Could not close", e);
            }
        }
    }

    public NanoHTTPD(int port) {
        this(null, port);
    }

    public NanoHTTPD(String hostname, int port) {
        this.hostname = hostname;
        this.myPort = port;
        this.setTempFileManagerFactory(new DefaultTempFileManagerFactory());
        this.setAsyncRunner(new DefaultAsyncRunner());
    }

    public synchronized void closeAllConnections() {
        this.stop();
    }

    protected ClientHandler createClientHandler(Socket finalAccept, InputStream inputStream) {
        return new ClientHandler(inputStream, finalAccept);
    }

    protected ServerRunnable createServerRunnable(int timeout) {
        return new ServerRunnable(timeout);
    }

    protected Map<String, List<String>> decodeParameters(Map<String, String> parms) {
        return this.decodeParameters(parms.get("NanoHttpd.QUERY_STRING"));
    }

    protected Map<String, List<String>> decodeParameters(String queryString) {
        HashMap<String, List<String>> parms = new HashMap<String, List<String>>();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String propertyName;
                String propertyValue;
                String e = st.nextToken();
                int sep = e.indexOf(61);
                String string = propertyName = sep >= 0 ? this.decodePercent(e.substring(0, sep)).trim() : this.decodePercent(e).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList());
                }
                if ((propertyValue = sep >= 0 ? this.decodePercent(e.substring(sep + 1)) : null) == null) continue;
                parms.get(propertyName).add(propertyValue);
            }
        }
        return parms;
    }

    protected String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        }
        catch (UnsupportedEncodingException ignored) {
            LOG.log(Level.WARNING, "Encoding not supported, ignored", ignored);
        }
        return decoded;
    }

    protected boolean useGzipWhenAccepted() {
        return true;
    }

    public final int getListeningPort() {
        return this.myServerSocket == null ? -1 : this.myServerSocket.getLocalPort();
    }

    public final boolean isAlive() {
        return this.wasStarted() && !this.myServerSocket.isClosed() && this.myThread.isAlive();
    }

    public void makeSecure(SSLServerSocketFactory sslServerSocketFactory) {
        this.sslServerSocketFactory = sslServerSocketFactory;
    }

    public Response newChunkedResponse(Response.IStatus status, String mimeType, InputStream data) {
        return new Response(status, mimeType, data, -1);
    }

    public Response newFixedLengthResponse(Response.IStatus status, String mimeType, InputStream data, long totalBytes) {
        return new Response(status, mimeType, data, totalBytes);
    }

    public Response newFixedLengthResponse(Response.IStatus status, String mimeType, String txt) {
        byte[] bytes;
        if (txt == null) {
            return this.newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        }
        try {
            bytes = txt.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "encoding problem, responding nothing", e);
            bytes = new byte[]{};
        }
        return this.newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(bytes), bytes.length);
    }

    public Response newFixedLengthResponse(String msg) {
        return this.newFixedLengthResponse(Response.Status.OK, "text/html", msg);
    }

    public Response serve(IHTTPSession session) {
        HashMap<String, String> files = new HashMap<String, String>();
        Method method = session.getMethod();
        if (Method.PUT.equals((Object)method) || Method.POST.equals((Object)method)) {
            try {
                session.parseBody(files);
            }
            catch (IOException ioe) {
                return this.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
            catch (ResponseException re) {
                return this.newFixedLengthResponse(re.getStatus(), "text/plain", re.getMessage());
            }
        }
        Map<String, String> parms = session.getParms();
        parms.put("NanoHttpd.QUERY_STRING", session.getQueryParameterString());
        return this.serve(session.getUri(), method, session.getHeaders(), parms, files);
    }

    @Deprecated
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        return this.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public void setAsyncRunner(AsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
    }

    public void setTempFileManagerFactory(TempFileManagerFactory tempFileManagerFactory) {
        this.tempFileManagerFactory = tempFileManagerFactory;
    }

    public void start() throws IOException {
        this.start(5000);
    }

    public void start(int timeout) throws IOException {
        if (this.sslServerSocketFactory != null) {
            SSLServerSocket ss = (SSLServerSocket)this.sslServerSocketFactory.createServerSocket();
            ss.setNeedClientAuth(false);
            this.myServerSocket = ss;
        } else {
            this.myServerSocket = new ServerSocket();
        }
        this.myServerSocket.setReuseAddress(true);
        ServerRunnable serverRunnable = this.createServerRunnable(timeout);
        this.myThread = new Thread(serverRunnable);
        this.myThread.setDaemon(true);
        this.myThread.setName("NanoHttpd Main Listener");
        this.myThread.start();
        while (!serverRunnable.hasBinded && serverRunnable.bindException == null) {
            try {
                Thread.sleep(10);
            }
            catch (Throwable var3_3) {}
        }
        if (serverRunnable.bindException != null) {
            throw serverRunnable.bindException;
        }
    }

    public void stop() {
        try {
            NanoHTTPD.safeClose(this.myServerSocket);
            this.asyncRunner.closeAll();
            if (this.myThread != null) {
                this.myThread.join();
            }
        }
        catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not stop all connections", e);
        }
    }

    public final boolean wasStarted() {
        return this.myServerSocket != null && this.myThread != null;
    }

    public static interface TempFileManagerFactory {
        public TempFileManager create();
    }

    public static interface TempFileManager {
        public void clear();

        public TempFile createTempFile() throws Exception;
    }

    public static interface TempFile {
        public void delete() throws Exception;

        public String getName();

        public OutputStream open() throws Exception;
    }

    public class ServerRunnable
    implements Runnable {
        private final int timeout;
        private IOException bindException;
        private boolean hasBinded;

        private ServerRunnable(int timeout) {
            this.hasBinded = false;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                NanoHTTPD.this.myServerSocket.bind(NanoHTTPD.this.hostname != null ? new InetSocketAddress(NanoHTTPD.this.hostname, NanoHTTPD.this.myPort) : new InetSocketAddress(NanoHTTPD.this.myPort));
                this.hasBinded = true;
            }
            catch (IOException e) {
                this.bindException = e;
                return;
            }
            do {
                try {
                    Socket finalAccept = NanoHTTPD.this.myServerSocket.accept();
                    if (this.timeout > 0) {
                        finalAccept.setSoTimeout(this.timeout);
                    }
                    InputStream inputStream = finalAccept.getInputStream();
                    NanoHTTPD.this.asyncRunner.exec(NanoHTTPD.this.createClientHandler(finalAccept, inputStream));
                    continue;
                }
                catch (IOException e) {
                    LOG.log(Level.FINE, "Communication with the client broken", e);
                }
            } while (!NanoHTTPD.this.myServerSocket.isClosed());
        }
    }

    public static final class ResponseException
    extends Exception {
        private static final long serialVersionUID = 6569838532917408380L;
        private final Response.Status status;

        public ResponseException(Response.Status status, String message) {
            super(message);
            this.status = status;
        }

        public ResponseException(Response.Status status, String message, Exception e) {
            super(message, e);
            this.status = status;
        }

        public Response.Status getStatus() {
            return this.status;
        }
    }

    public static class Response {
        private IStatus status;
        private String mimeType;
        private InputStream data;
        private long contentLength;
        private final Map<String, String> header = new HashMap<String, String>();
        private Method requestMethod;
        private boolean chunkedTransfer;
        private boolean encodeAsGzip;
        private boolean keepAlive;

        protected Response(IStatus status, String mimeType, InputStream data, long totalBytes) {
            this.status = status;
            this.mimeType = mimeType;
            if (data == null) {
                this.data = new ByteArrayInputStream(new byte[0]);
                this.contentLength = 0;
            } else {
                this.data = data;
                this.contentLength = totalBytes;
            }
            this.chunkedTransfer = this.contentLength < 0;
            this.keepAlive = true;
        }

        public void addHeader(String name, String value) {
            this.header.put(name, value);
        }

        public InputStream getData() {
            return this.data;
        }

        public String getHeader(String name) {
            for (String headerName : this.header.keySet()) {
                if (!headerName.equalsIgnoreCase(name)) continue;
                return this.header.get(headerName);
            }
            return null;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public Method getRequestMethod() {
            return this.requestMethod;
        }

        public IStatus getStatus() {
            return this.status;
        }

        public void setGzipEncoding(boolean encodeAsGzip) {
            this.encodeAsGzip = encodeAsGzip;
        }

        public void setKeepAlive(boolean useKeepAlive) {
            this.keepAlive = useKeepAlive;
        }

        private boolean headerAlreadySent(Map<String, String> header, String name) {
            boolean alreadySent = false;
            for (String headerName : header.keySet()) {
                alreadySent |= headerName.equalsIgnoreCase(name);
            }
            return alreadySent;
        }

        protected void send(OutputStream outputStream) {
            String mime = this.mimeType;
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                long pending;
                if (this.status == null) {
                    throw new Error("sendResponse(): Status can't be null.");
                }
                PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
                pw.print("HTTP/1.1 " + this.status.getDescription() + " \r\n");
                if (mime != null) {
                    pw.print("Content-Type: " + mime + "\r\n");
                }
                if (this.header == null || this.header.get("Date") == null) {
                    pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                }
                if (this.header != null) {
                    for (String key : this.header.keySet()) {
                        String value = this.header.get(key);
                        pw.print(key + ": " + value + "\r\n");
                    }
                }
                if (!this.headerAlreadySent(this.header, "connection")) {
                    pw.print("Connection: " + (this.keepAlive ? "keep-alive" : "close") + "\r\n");
                }
                if (this.headerAlreadySent(this.header, "content-length")) {
                    this.encodeAsGzip = false;
                }
                if (this.encodeAsGzip) {
                    pw.print("Content-Encoding: gzip\r\n");
                    this.setChunkedTransfer(true);
                }
                long l = pending = this.data != null ? this.contentLength : 0;
                if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                    pw.print("Transfer-Encoding: chunked\r\n");
                } else if (!this.encodeAsGzip) {
                    pending = this.sendContentLengthHeaderIfNotAlreadyPresent(pw, this.header, pending);
                }
                pw.print("\r\n");
                pw.flush();
                this.sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
                outputStream.flush();
                NanoHTTPD.safeClose(this.data);
            }
            catch (IOException ioe) {
                LOG.log(Level.SEVERE, "Could not send response to the client", ioe);
            }
        }

        private void sendBodyWithCorrectTransferAndEncoding(OutputStream outputStream, long pending) throws IOException {
            if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
                this.sendBodyWithCorrectEncoding(chunkedOutputStream, -1);
                chunkedOutputStream.finish();
            } else {
                this.sendBodyWithCorrectEncoding(outputStream, pending);
            }
        }

        private void sendBodyWithCorrectEncoding(OutputStream outputStream, long pending) throws IOException {
            if (this.encodeAsGzip) {
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                this.sendBody(gzipOutputStream, -1);
                gzipOutputStream.finish();
            } else {
                this.sendBody(outputStream, pending);
            }
        }

        private void sendBody(OutputStream outputStream, long pending) throws IOException {
            long bytesToRead;
            boolean sendEverything;
            int read;
            long BUFFER_SIZE = 16384;
            byte[] buff = new byte[(int)BUFFER_SIZE];
            boolean bl = sendEverything = pending == -1;
            while ((pending > 0 || sendEverything) && (read = this.data.read(buff, 0, (int)(bytesToRead = sendEverything ? BUFFER_SIZE : Math.min(pending, BUFFER_SIZE)))) > 0) {
                outputStream.write(buff, 0, read);
                if (sendEverything) continue;
                pending -= (long)read;
            }
        }

        protected long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header, long size) {
            for (String headerName : header.keySet()) {
                if (!headerName.equalsIgnoreCase("content-length")) continue;
                try {
                    return Long.parseLong(header.get(headerName));
                }
                catch (NumberFormatException ex) {
                    return size;
                }
            }
            pw.print("Content-Length: " + size + "\r\n");
            return size;
        }

        public void setChunkedTransfer(boolean chunkedTransfer) {
            this.chunkedTransfer = chunkedTransfer;
        }

        public void setData(InputStream data) {
            this.data = data;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public void setRequestMethod(Method requestMethod) {
            this.requestMethod = requestMethod;
        }

        public void setStatus(IStatus status) {
            this.status = status;
        }

        private static class ChunkedOutputStream
        extends FilterOutputStream {
            public ChunkedOutputStream(OutputStream out) {
                super(out);
            }

            @Override
            public void write(int b) throws IOException {
                byte[] data = new byte[]{(byte)b};
                this.write(data, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (len == 0) {
                    return;
                }
                this.out.write(String.format("%x\r\n", len).getBytes());
                this.out.write(b, off, len);
                this.out.write("\r\n".getBytes());
            }

            public void finish() throws IOException {
                this.out.write("0\r\n\r\n".getBytes());
            }
        }

        public static enum Status implements IStatus
        {
            SWITCH_PROTOCOL(101, "Switching Protocols"),
            OK(200, "OK"),
            CREATED(201, "Created"),
            ACCEPTED(202, "Accepted"),
            NO_CONTENT(204, "No Content"),
            PARTIAL_CONTENT(206, "Partial Content"),
            REDIRECT(301, "Moved Permanently"),
            NOT_MODIFIED(304, "Not Modified"),
            BAD_REQUEST(400, "Bad Request"),
            UNAUTHORIZED(401, "Unauthorized"),
            FORBIDDEN(403, "Forbidden"),
            NOT_FOUND(404, "Not Found"),
            METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
            RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
            INTERNAL_ERROR(500, "Internal Server Error"),
            UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported");
            
            private final int requestStatus;
            private final String description;

            private Status(int requestStatus, String description) {
                this.requestStatus = requestStatus;
                this.description = description;
            }

            @Override
            public String getDescription() {
                return "" + this.requestStatus + " " + this.description;
            }

            @Override
            public int getRequestStatus() {
                return this.requestStatus;
            }
        }

        public static interface IStatus {
            public String getDescription();

            public int getRequestStatus();
        }

    }

    public static enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        HEAD,
        OPTIONS;
        

        private Method() {
        }

        static Method lookup(String method) {
            for (Method m : Method.values()) {
                if (!m.toString().equalsIgnoreCase(method)) continue;
                return m;
            }
            return null;
        }
    }

    public static interface IHTTPSession {
        public void execute() throws IOException;

        public CookieHandler getCookies();

        public Map<String, String> getHeaders();

        public InputStream getInputStream();

        public Method getMethod();

        public Map<String, String> getParms();

        public String getQueryParameterString();

        public String getUri();

        public void parseBody(Map<String, String> var1) throws IOException, ResponseException;
    }

    protected class HTTPSession
    implements IHTTPSession {
        public static final int BUFSIZE = 8192;
        private final TempFileManager tempFileManager;
        private final OutputStream outputStream;
        private final PushbackInputStream inputStream;
        private int splitbyte;
        private int rlen;
        private String uri;
        private Method method;
        private Map<String, String> parms;
        private Map<String, String> headers;
        private CookieHandler cookies;
        private String queryParameterString;
        private String remoteIp;
        private String protocolVersion;
        final /* synthetic */ NanoHTTPD this$0;

        public HTTPSession(NanoHTTPD nanoHTTPD, TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream) {
            this.this$0 = nanoHTTPD;
            this.tempFileManager = tempFileManager;
            this.inputStream = new PushbackInputStream(inputStream, 8192);
            this.outputStream = outputStream;
        }

        public HTTPSession(NanoHTTPD nanoHTTPD, TempFileManager tempFileManager, InputStream inputStream, OutputStream outputStream, InetAddress inetAddress) {
            this.this$0 = nanoHTTPD;
            this.tempFileManager = tempFileManager;
            this.inputStream = new PushbackInputStream(inputStream, 8192);
            this.outputStream = outputStream;
            this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
            this.headers = new HashMap<String, String>();
        }

        private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, String> parms, Map<String, String> headers) throws ResponseException {
            try {
                String inLine = in.readLine();
                if (inLine == null) {
                    return;
                }
                StringTokenizer st = new StringTokenizer(inLine);
                if (!st.hasMoreTokens()) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }
                pre.put("method", st.nextToken());
                if (!st.hasMoreTokens()) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }
                String uri = st.nextToken();
                int qmi = uri.indexOf(63);
                if (qmi >= 0) {
                    this.decodeParms(uri.substring(qmi + 1), parms);
                    uri = this.this$0.decodePercent(uri.substring(0, qmi));
                } else {
                    uri = this.this$0.decodePercent(uri);
                }
                if (st.hasMoreTokens()) {
                    this.protocolVersion = st.nextToken();
                } else {
                    this.protocolVersion = "HTTP/1.1";
                    LOG.log(Level.FINE, "no protocol version specified, strange. Assuming HTTP/1.1.");
                }
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(58); // char ':'
                    if (p >= 0) {
                        headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                    }
                    line = in.readLine();
                }
                pre.put("uri", uri);
            }
            catch (IOException ioe) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
            }
        }

        private void decodeMultipartFormData(String boundary, ByteBuffer fbuf, Map<String, String> parms, Map<String, String> files) throws ResponseException {
            try {
                int[] boundary_idxs = this.getBoundaryPositions(fbuf, boundary.getBytes());
                if (boundary_idxs.length < 2) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
                }
                int MAX_HEADER_SIZE = 1024;
                byte[] part_header_buff = new byte[1024];
                for (int bi = 0; bi < boundary_idxs.length - 1; ++bi) {
                    fbuf.position(boundary_idxs[bi]);
                    int len = fbuf.remaining() < 1024 ? fbuf.remaining() : 1024;
                    fbuf.get(part_header_buff, 0, len);
                    ByteArrayInputStream bais = new ByteArrayInputStream(part_header_buff, 0, len);
                    BufferedReader in = new BufferedReader(new InputStreamReader(bais));
                    String mpline = in.readLine();
                    if (!mpline.contains(boundary)) {
                        throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                    }
                    String part_name = null;
                    String file_name = null;
                    String content_type = null;
                    mpline = in.readLine();
                    while (mpline != null && mpline.trim().length() > 0) {
                        Matcher matcher = CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                        if (matcher.matches()) {
                            String attributeString = matcher.group(2);
                            matcher = CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                            while (matcher.find()) {
                                String key = matcher.group(1);
                                if (key.equalsIgnoreCase("name")) {
                                    part_name = matcher.group(2);
                                    continue;
                                }
                                if (!key.equalsIgnoreCase("filename")) continue;
                                file_name = matcher.group(2);
                            }
                        }
                        if ((matcher = CONTENT_TYPE_PATTERN.matcher(mpline)).matches()) {
                            content_type = matcher.group(2).trim();
                        }
                        mpline = in.readLine();
                    }
                    int part_header_len = len - (int)in.skip(1024);
                    if (part_header_len >= len - 4) {
                        throw new ResponseException(Response.Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                    }
                    int part_data_start = boundary_idxs[bi] + part_header_len;
                    int part_data_end = boundary_idxs[bi + 1] - 4;
                    fbuf.position(part_data_start);
                    if (content_type == null) {
                        byte[] data_bytes = new byte[part_data_end - part_data_start];
                        fbuf.get(data_bytes);
                        parms.put(part_name, new String(data_bytes));
                        continue;
                    }
                    String path = this.saveTmpFile(fbuf, part_data_start, part_data_end - part_data_start);
                    if (!files.containsKey(part_name)) {
                        files.put(part_name, path);
                    } else {
                        int count = 2;
                        while (files.containsKey(part_name + count)) {
                            ++count;
                        }
                        files.put(part_name + count, path);
                    }
                    parms.put(part_name, file_name);
                }
            }
            catch (ResponseException re) {
                throw re;
            }
            catch (Exception e) {
                throw new ResponseException(Response.Status.INTERNAL_ERROR, e.toString());
            }
        }

        private void decodeParms(String parms, Map<String, String> p) {
            if (parms == null) {
                this.queryParameterString = "";
                return;
            }
            this.queryParameterString = parms;
            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf(61);
                if (sep >= 0) {
                    p.put(this.this$0.decodePercent(e.substring(0, sep)).trim(), this.this$0.decodePercent(e.substring(sep + 1)));
                    continue;
                }
                p.put(this.this$0.decodePercent(e).trim(), "");
            }
        }

        @Override
        public void execute() throws IOException {
            try {
                byte[] buf = new byte[8192];
                this.splitbyte = 0;
                this.rlen = 0;
                int read = -1;
                try {
                    read = this.inputStream.read(buf, 0, 8192);
                }
                catch (Exception e) {
                    NanoHTTPD.safeClose(this.inputStream);
                    NanoHTTPD.safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                }
                if (read == -1) {
                    NanoHTTPD.safeClose(this.inputStream);
                    NanoHTTPD.safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                }
                while (read > 0) {
                    this.rlen += read;
                    this.splitbyte = this.findHeaderEnd(buf, this.rlen);
                    if (this.splitbyte > 0) break;
                    read = this.inputStream.read(buf, this.rlen, 8192 - this.rlen);
                }
                if (this.splitbyte < this.rlen) {
                    this.inputStream.unread(buf, this.splitbyte, this.rlen - this.splitbyte);
                }
                this.parms = new HashMap<String, String>();
                if (null == this.headers) {
                    this.headers = new HashMap<String, String>();
                } else {
                    this.headers.clear();
                }
                if (null != this.remoteIp) {
                    this.headers.put("remote-addr", this.remoteIp);
                    this.headers.put("http-client-ip", this.remoteIp);
                }
                BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));
                HashMap<String, String> pre = new HashMap<String, String>();
                this.decodeHeader(hin, pre, this.parms, this.headers);
                this.method = Method.lookup(pre.get("method"));
                if (this.method == null) {
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
                }
                this.uri = pre.get("uri");
                this.cookies = this.this$0.new CookieHandler(this.headers);
                String connection = this.headers.get("connection");
                boolean keepAlive = this.protocolVersion.equals("HTTP/1.1") && (connection == null || !connection.matches("(?i).*close.*"));
                Response r = this.this$0.serve(this);
                if (r == null) {
                    throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                }
                String acceptEncoding = this.headers.get("accept-encoding");
                this.cookies.unloadQueue(r);
                r.setRequestMethod(this.method);
                r.setGzipEncoding(this.this$0.useGzipWhenAccepted() && acceptEncoding != null && acceptEncoding.contains("gzip"));
                r.setKeepAlive(keepAlive);
                r.send(this.outputStream);
                if (!keepAlive || "close".equalsIgnoreCase(r.getHeader("connection"))) {
                    throw new SocketException("NanoHttpd Shutdown");
                }
            }
            catch (SocketException e) {
                throw e;
            }
            catch (SocketTimeoutException ste) {
                throw ste;
            }
            catch (IOException ioe) {
                Response r = this.this$0.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                r.send(this.outputStream);
                NanoHTTPD.safeClose(this.outputStream);
            }
            catch (ResponseException re) {
                Response r = this.this$0.newFixedLengthResponse(re.getStatus(), "text/plain", re.getMessage());
                r.send(this.outputStream);
                NanoHTTPD.safeClose(this.outputStream);
            }
            finally {
                this.tempFileManager.clear();
            }
        }

        private int findHeaderEnd(byte[] buf, int rlen) {
            int splitbyte = 0;
            while (splitbyte + 3 < rlen) {
		// ascii 13=carriagereturn 10=newline 
                if (buf[splitbyte] == 13 && buf[splitbyte + 1] == 10 && buf[splitbyte + 2] == 13 && buf[splitbyte + 3] == 10) {
                    return splitbyte + 4;
                }
                ++splitbyte;
            }
            return 0;
        }

        private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
            int[] res = new int[]{};
            if (b.remaining() < boundary.length) {
                return res;
            }
            int search_window_pos = 0;
            byte[] search_window = new byte[4096 + boundary.length];
            int first_fill = b.remaining() < search_window.length ? b.remaining() : search_window.length;
            b.get(search_window, 0, first_fill);
            int new_bytes = first_fill - boundary.length;
            do {
                for (int j = 0; j < new_bytes; ++j) {
                    for (int i = 0; i < boundary.length && search_window[j + i] == boundary[i]; ++i) {
                        if (i != boundary.length - 1) continue;
                        int[] new_res = new int[res.length + 1];
                        System.arraycopy(res, 0, new_res, 0, res.length);
                        new_res[res.length] = search_window_pos + j;
                        res = new_res;
                    }
                }
                search_window_pos += new_bytes;
                System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);
                new_bytes = search_window.length - boundary.length;
                new_bytes = b.remaining() < new_bytes ? b.remaining() : new_bytes;
                b.get(search_window, boundary.length, new_bytes);
            } while (new_bytes > 0);
            return res;
        }

        @Override
        public CookieHandler getCookies() {
            return this.cookies;
        }

        @Override
        public final Map<String, String> getHeaders() {
            return this.headers;
        }

        @Override
        public final InputStream getInputStream() {
            return this.inputStream;
        }

        @Override
        public final Method getMethod() {
            return this.method;
        }

        @Override
        public final Map<String, String> getParms() {
            return this.parms;
        }

        @Override
        public String getQueryParameterString() {
            return this.queryParameterString;
        }

        private RandomAccessFile getTmpBucket() {
            try {
                TempFile tempFile = this.tempFileManager.createTempFile();
                return new RandomAccessFile(tempFile.getName(), "rw");
            }
            catch (Exception e) {
                throw new Error(e);
            }
        }

        @Override
        public final String getUri() {
            return this.uri;
        }

        @Override
        public void parseBody(Map<String, String> files) throws IOException, ResponseException {
            int REQUEST_BUFFER_LEN = 512;
            int MEMORY_STORE_LIMIT = 1024;
            RandomAccessFile randomAccessFile = null;
            try {
                long size = this.headers.containsKey("content-length") ? (long)Integer.parseInt(this.headers.get("content-length")) : (this.splitbyte < this.rlen ? (long)(this.rlen - this.splitbyte) : 0);
                ByteArrayOutputStream baos = null;
                DataOutput request_data_output = null;
                if (size < 1024) {
                    baos = new ByteArrayOutputStream();
                    request_data_output = new DataOutputStream(baos);
                } else {
                    request_data_output = randomAccessFile = this.getTmpBucket();
                }
                byte[] buf = new byte[512];
                while (this.rlen >= 0 && size > 0) {
                    this.rlen = this.inputStream.read(buf, 0, (int)Math.min(size, 512));
                    size -= (long)this.rlen;
                    if (this.rlen <= 0) continue;
                    request_data_output.write(buf, 0, this.rlen);
                }
                ByteBuffer fbuf = null;
                if (baos != null) {
                    fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
                } else {
                    fbuf = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
                    randomAccessFile.seek(0);
                }
                if (Method.POST.equals((Object)this.method)) {
                    String contentType = "";
                    String contentTypeHeader = this.headers.get("content-type");
                    StringTokenizer st = null;
                    if (contentTypeHeader != null && (st = new StringTokenizer(contentTypeHeader, ",; ")).hasMoreTokens()) {
                        contentType = st.nextToken();
                    }
                    if ("multipart/form-data".equalsIgnoreCase(contentType)) {
                        if (!st.hasMoreTokens()) {
                            throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                        }
                        String boundaryStartString = "boundary=";
                        int boundaryContentStart = contentTypeHeader.indexOf(boundaryStartString) + boundaryStartString.length();
                        String boundary = contentTypeHeader.substring(boundaryContentStart, contentTypeHeader.length());
                        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                            boundary = boundary.substring(1, boundary.length() - 1);
                        }
                        this.decodeMultipartFormData(boundary, fbuf, this.parms, files);
                    } else {
                        byte[] postBytes = new byte[fbuf.remaining()];
                        fbuf.get(postBytes);
                        String postLine = new String(postBytes).trim();
                        if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                            this.decodeParms(postLine, this.parms);
                        } else if (postLine.length() != 0) {
                            files.put("postData", postLine);
                        }
                    }
                } else if (Method.PUT.equals((Object)this.method)) {
                    files.put("content", this.saveTmpFile(fbuf, 0, fbuf.limit()));
                }
            }
            finally {
                NanoHTTPD.safeClose(randomAccessFile);
            }
        }

        private String saveTmpFile(ByteBuffer b, int offset, int len) {
            String path = "";
            if (len > 0) {
                FileOutputStream fileOutputStream = null;
                try {
                    TempFile tempFile = this.tempFileManager.createTempFile();
                    ByteBuffer src = b.duplicate();
                    fileOutputStream = new FileOutputStream(tempFile.getName());
                    FileChannel dest = fileOutputStream.getChannel();
                    src.position(offset).limit(offset + len);
                    dest.write(src.slice());
                    path = tempFile.getName();
                }
                catch (Exception e) {
                    throw new Error(e);
                }
                finally {
                    NanoHTTPD.safeClose(fileOutputStream);
                }
            }
            return path;
        }
    }

    private class DefaultTempFileManagerFactory
    implements TempFileManagerFactory {
        private DefaultTempFileManagerFactory() {
        }

        @Override
        public TempFileManager create() {
            return new DefaultTempFileManager();
        }
    }

    public static class DefaultTempFileManager
    implements TempFileManager {
        private final String tmpdir = System.getProperty("java.io.tmpdir");
        private final List<TempFile> tempFiles = new ArrayList<TempFile>();

        @Override
        public void clear() {
            for (TempFile file : this.tempFiles) {
                try {
                    file.delete();
                }
                catch (Exception ignored) {
                    LOG.log(Level.WARNING, "could not delete file ", ignored);
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public TempFile createTempFile() throws Exception {
            DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            return tempFile;
        }
    }

    public static class DefaultTempFile
    implements TempFile {
        private final File file;
        private final OutputStream fstream;

        public DefaultTempFile(String tempdir) throws IOException {
            this.file = File.createTempFile("NanoHTTPD-", "", new File(tempdir));
            this.fstream = new FileOutputStream(this.file);
        }

        @Override
        public void delete() throws Exception {
            NanoHTTPD.safeClose(this.fstream);
            if (!this.file.delete()) {
                throw new Exception("could not delete temporary file");
            }
        }

        @Override
        public String getName() {
            return this.file.getAbsolutePath();
        }

        @Override
        public OutputStream open() throws Exception {
            return this.fstream;
        }
    }

    public static class DefaultAsyncRunner
    implements AsyncRunner {
        private long requestCount;
        private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList());

        public List<ClientHandler> getRunning() {
            return this.running;
        }

        @Override
        public void closeAll() {
            for (ClientHandler clientHandler : new ArrayList<ClientHandler>(this.running)) {
                clientHandler.close();
            }
        }

        @Override
        public void closed(ClientHandler clientHandler) {
            this.running.remove(clientHandler);
        }

        @Override
        public void exec(ClientHandler clientHandler) {
            ++this.requestCount;
            Thread t = new Thread(clientHandler);
            t.setDaemon(true);
            t.setName("NanoHttpd Request Processor (#" + this.requestCount + ")");
            this.running.add(clientHandler);
            t.start();
        }
    }

    public class CookieHandler
    implements Iterable<String> {
        private final HashMap<String, String> cookies;
        private final ArrayList<Cookie> queue;

        public CookieHandler(Map<String, String> httpHeaders) {
            this.cookies = new HashMap();
            this.queue = new ArrayList();
            String raw = httpHeaders.get("cookie");
            if (raw != null) {
                String[] tokens;
                for (String token : tokens = raw.split(";")) {
                    String[] data = token.trim().split("=");
                    if (data.length != 2) continue;
                    this.cookies.put(data[0], data[1]);
                }
            }
        }

        public void delete(String name) {
            this.set(name, "-delete-", -30);
        }

        @Override
        public Iterator<String> iterator() {
            return this.cookies.keySet().iterator();
        }

        public String read(String name) {
            return this.cookies.get(name);
        }

        public void set(Cookie cookie) {
            this.queue.add(cookie);
        }

        public void set(String name, String value, int expires) {
            this.queue.add(new Cookie(name, value, Cookie.getHTTPTime(expires)));
        }

        public void unloadQueue(Response response) {
            for (Cookie cookie : this.queue) {
                response.addHeader("Set-Cookie", cookie.getHTTPHeader());
            }
        }
    }

    public static class Cookie {
        private final String n;
        private final String v;
        private final String e;

        public static String getHTTPTime(int days) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.add(5, days);
            return dateFormat.format(calendar.getTime());
        }

        public Cookie(String name, String value) {
            this(name, value, 30);
        }

        public Cookie(String name, String value, int numDays) {
            this.n = name;
            this.v = value;
            this.e = Cookie.getHTTPTime(numDays);
        }

        public Cookie(String name, String value, String expires) {
            this.n = name;
            this.v = value;
            this.e = expires;
        }

        public String getHTTPHeader() {
            String fmt = "%s=%s; expires=%s";
            return String.format(fmt, this.n, this.v, this.e);
        }
    }

    public class ClientHandler
    implements Runnable {
        private final InputStream inputStream;
        private final Socket acceptSocket;

        private ClientHandler(InputStream inputStream, Socket acceptSocket) {
            this.inputStream = inputStream;
            this.acceptSocket = acceptSocket;
        }

        public void close() {
            NanoHTTPD.safeClose(this.inputStream);
            NanoHTTPD.safeClose(this.acceptSocket);
        }

        @Override
        public void run() {
            OutputStream outputStream = null;
            try {
                outputStream = this.acceptSocket.getOutputStream();
                TempFileManager tempFileManager = NanoHTTPD.this.tempFileManagerFactory.create();
                HTTPSession session = new HTTPSession(NanoHTTPD.this, tempFileManager, this.inputStream, outputStream, this.acceptSocket.getInetAddress());
                while (!this.acceptSocket.isClosed()) {
                    session.execute();
                }
            }
            catch (Exception e) {
                if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage()) || e instanceof SocketTimeoutException)) {
                    LOG.log(Level.FINE, "Communication with the client broken", e);
                }
            }
            finally {
                NanoHTTPD.safeClose(outputStream);
                NanoHTTPD.safeClose(this.inputStream);
                NanoHTTPD.safeClose(this.acceptSocket);
                NanoHTTPD.this.asyncRunner.closed(this);
            }
        }
    }

    public static interface AsyncRunner {
        public void closeAll();

        public void closed(ClientHandler var1);

        public void exec(ClientHandler var1);
    }

}

