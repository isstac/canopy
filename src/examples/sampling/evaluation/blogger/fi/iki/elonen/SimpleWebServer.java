/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

public class SimpleWebServer
extends NanoHTTPD {
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>(){};
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>(){};
    private static final String LICENCE = "Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
    private static Map<String, WebServerPlugin> mimeTypeHandlers = new HashMap<String, WebServerPlugin>();
    private final boolean quiet;
    protected List<File> rootDirs;

    public static void main(String[] args) {
        int port = 8080;
        String host = null;
        ArrayList<File> rootDirs = new ArrayList<File>();
        boolean quiet = false;
        HashMap<String, String> options = new HashMap<String, String>();
        for (int i = 0; i < args.length; ++i) {
            int dot;
            if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--host")) {
                host = args[i + 1];
                continue;
            }
            if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("--port")) {
                port = Integer.parseInt(args[i + 1]);
                continue;
            }
            if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--quiet")) {
                quiet = true;
                continue;
            }
            if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dir")) {
                rootDirs.add(new File(args[i + 1]).getAbsoluteFile());
                continue;
            }
            if (args[i].equalsIgnoreCase("--licence")) {
                System.out.println("Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias\n\nRedistribution and use in source and binary forms, with or without\nmodification, are permitted provided that the following conditions\nare met:\n\nRedistributions of source code must retain the above copyright notice,\nthis list of conditions and the following disclaimer. Redistributions in\nbinary form must reproduce the above copyright notice, this list of\nconditions and the following disclaimer in the documentation and/or other\nmaterials provided with the distribution. The name of the author may not\nbe used to endorse or promote products derived from this software without\nspecific prior written permission. \n \nTHIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\nIMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\nOF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\nIN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\nINCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\nNOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\nDATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\nTHEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n");
                continue;
            }
            if (!args[i].startsWith("-X:") || (dot = args[i].indexOf(61)) <= 0) continue;
            String name = args[i].substring(0, dot);
            String value = args[i].substring(dot + 1, args[i].length());
            options.put(name, value);
        }
        if (rootDirs.isEmpty()) {
            rootDirs.add(new File(".").getAbsoluteFile());
        }
        options.put("host", host);
        options.put("port", "" + port);
        options.put("quiet", String.valueOf(quiet));
        StringBuilder sb = new StringBuilder();
        for (File dir : rootDirs) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            try {
                sb.append(dir.getCanonicalPath());
            }
            catch (IOException value) {}
        }
        options.put("home", sb.toString());
        ServiceLoader<WebServerPluginInfo> serviceLoader = ServiceLoader.load(WebServerPluginInfo.class);
        for (WebServerPluginInfo info : serviceLoader) {
            String[] mimeTypes;
            for (String mime : mimeTypes = info.getMimeTypes()) {
                String[] indexFiles = info.getIndexFilesForMimeType(mime);
                if (!quiet) {
                    System.out.print("# Found plugin for Mime type: \"" + mime + "\"");
                    if (indexFiles != null) {
                        System.out.print(" (serving index files: ");
                        for (String indexFile : indexFiles) {
                            System.out.print(indexFile + " ");
                        }
                    }
                    System.out.println(").");
                }
                SimpleWebServer.registerPluginForMimeType(indexFiles, mime, info.getWebServerPlugin(mime), options);
            }
        }
        ServerRunner.executeInstance(new SimpleWebServer(host, port, rootDirs, quiet));
    }

    protected static void registerPluginForMimeType(String[] indexFiles, String mimeType, WebServerPlugin plugin, Map<String, String> commandLineOptions) {
        if (mimeType == null || plugin == null) {
            return;
        }
        if (indexFiles != null) {
            for (String filename : indexFiles) {
                int dot = filename.lastIndexOf(46);
                if (dot < 0) continue;
                String extension = filename.substring(dot + 1).toLowerCase();
                MIME_TYPES.put(extension, mimeType);
            }
            INDEX_FILE_NAMES.addAll(Arrays.asList(indexFiles));
        }
        mimeTypeHandlers.put(mimeType, plugin);
        plugin.initialize(commandLineOptions);
    }

    public SimpleWebServer(String host, int port, File wwwroot, boolean quiet) {
        super(host, port);
        this.quiet = quiet;
        this.rootDirs = new ArrayList<File>();
        this.rootDirs.add(wwwroot);
        this.init();
    }

    public SimpleWebServer(String host, int port, List<File> wwwroots, boolean quiet) {
        super(host, port);
        this.quiet = quiet;
        this.rootDirs = new ArrayList<File>(wwwroots);
        this.init();
    }

    private boolean canServeUri(String uri, File homeDir) {
        String mimeTypeForFile;
        WebServerPlugin plugin;
        File f = new File(homeDir, uri);
        boolean canServeUri = f.exists();
        if (!canServeUri && (plugin = mimeTypeHandlers.get(mimeTypeForFile = this.getMimeTypeForFile(uri))) != null) {
            canServeUri = plugin.canServeUri(uri, homeDir);
        }
        return canServeUri;
    }

    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri = newUri + "/";
                continue;
            }
            if (tok.equals(" ")) {
                newUri = newUri + "%20";
                continue;
            }
            try {
                newUri = newUri + URLEncoder.encode(tok, "UTF-8");
            }
            catch (UnsupportedEncodingException var5_5) {}
        }
        return newUri;
    }

    private String findIndexFileInDirectory(File directory) {
        for (String fileName : INDEX_FILE_NAMES) {
            File indexFile = new File(directory, fileName);
            if (!indexFile.isFile()) continue;
            return fileName;
        }
        return null;
    }

    protected NanoHTTPD.Response getForbiddenResponse(String s) {
        return this.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, "text/plain", "FORBIDDEN: " + s);
    }

    protected NanoHTTPD.Response getInternalErrorResponse(String s) {
        return this.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "INTERNAL ERROR: " + s);
    }

    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf(46);
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? "application/octet-stream" : mime;
    }

    protected NanoHTTPD.Response getNotFoundResponse() {
        return this.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "Error 404, file not found.");
    }

    public void init() {
    }

    protected String listDirectory(String uri, File f) {
        String u;
        int slash;
        String heading = "Directory " + uri;
        StringBuilder msg = new StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" + "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n" + "// -->\n" + "</style>" + "</head><body><h1>" + heading + "</h1>");
        String up = null;
        if (uri.length() > 1 && (slash = (u = uri.substring(0, uri.length() - 1)).lastIndexOf(47)) >= 0 && slash < u.length()) {
            up = uri.substring(0, slash + 1);
        }
        List<String> files = Arrays.asList(f.list(new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg.append("<ul>");
            if (up != null || directories.size() > 0) {
                msg.append("<section class=\"directories\">");
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>");
                }
                for (String directory : directories) {
                    String dir = directory + "/";
                    msg.append("<li><a rel=\"directory\" href=\"").append(this.encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></b></li>");
                }
                msg.append("</section>");
            }
            if (files.size() > 0) {
                msg.append("<section class=\"files\">");
                for (String file : files) {
                    msg.append("<li><a href=\"").append(this.encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg.append("&nbsp;<span class=\"filesize\">(");
                    if (len < 1024) {
                        msg.append(len).append(" bytes");
                    } else if (len < 0x100000) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                    } else {
                        msg.append(len / 0x100000).append(".").append(len % 0x100000 / 10000 % 100).append(" MB");
                    }
                    msg.append(")</span></li>");
                }
                msg.append("</section>");
            }
            msg.append("</ul>");
        }
        msg.append("</body></html>");
        return msg.toString();
    }

    @Override
    public NanoHTTPD.Response newFixedLengthResponse(NanoHTTPD.Response.IStatus status, String mimeType, String message) {
        NanoHTTPD.Response response = super.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    private NanoHTTPD.Response respond(Map<String, String> headers, NanoHTTPD.IHTTPSession session, String uri) {
        if ((uri = uri.trim().replace(File.separatorChar, '/')).indexOf(63) >= 0) {
            uri = uri.substring(0, uri.indexOf(63));
        }
        if (uri.contains("../")) {
            return this.getForbiddenResponse("Won't serve ../ for security reasons.");
        }
        boolean canServeUri = false;
        File homeDir = null;
        for (int i = 0; !canServeUri && i < this.rootDirs.size(); ++i) {
            homeDir = this.rootDirs.get(i);
            canServeUri = this.canServeUri(uri, homeDir);
        }
        if (!canServeUri) {
            return this.getNotFoundResponse();
        }
        File f = new File(homeDir, uri);
        if (f.isDirectory() && !uri.endsWith("/")) {
            uri = uri + "/";
            NanoHTTPD.Response res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.REDIRECT, "text/html", "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }
        if (f.isDirectory()) {
            String indexFile = this.findIndexFileInDirectory(f);
            if (indexFile == null) {
                if (f.canRead()) {
                    return this.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", this.listDirectory(uri, f));
                }
                return this.getForbiddenResponse("No directory listing.");
            }
            return this.respond(headers, session, uri + indexFile);
        }
        String mimeTypeForFile = this.getMimeTypeForFile(uri);
        WebServerPlugin plugin = mimeTypeHandlers.get(mimeTypeForFile);
        NanoHTTPD.Response response = null;
        if (plugin != null && plugin.canServeUri(uri, homeDir)) {
            response = plugin.serveFile(uri, headers, session, f, mimeTypeForFile);
            if (response != null && response instanceof InternalRewrite) {
                InternalRewrite rewrite = (InternalRewrite)response;
                return this.respond(rewrite.getHeaders(), session, rewrite.getUri());
            }
        } else {
            response = this.serveFile(uri, headers, f, mimeTypeForFile);
        }
        return response != null ? response : this.getNotFoundResponse();
    }

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();
        if (!this.quiet) {
            System.out.println((Object)((Object)session.getMethod()) + " '" + uri + "' ");
            for (String value2 : header.keySet()) {
                System.out.println("  HDR: '" + value2 + "' = '" + header.get(value2) + "'");
            }
            for (String value2 : parms.keySet()) {
                System.out.println("  PRM: '" + value2 + "' = '" + parms.get(value2) + "'");
            }
        }
        for (File homeDir : this.rootDirs) {
            if (homeDir.isDirectory()) continue;
            return this.getInternalErrorResponse("given path is not a directory (" + homeDir + ").");
        }
        return this.respond(Collections.unmodifiableMap(header), session, uri);
    }

    NanoHTTPD.Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        NanoHTTPD.Response res;
        try {
            String ifRange;
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null && range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf(45);
                try {
                    if (minus > 0) {
                        startFrom = Long.parseLong(range.substring(0, minus));
                        endAt = Long.parseLong(range.substring(minus + 1));
                    }
                }
                catch (NumberFormatException var13_12) {
                    // empty catch block
                }
            }
            boolean headerIfRangeMissingOrMatching = (ifRange = header.get("if-range")) == null || etag.equals(ifRange);
            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag));
            long fileLen = file.length();
            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                if (headerIfNoneMatchPresentAndMatching) {
                    res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    long newLen;
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    if ((newLen = endAt - startFrom + 1) < 0) {
                        newLen = 0;
                    }
                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);
                    res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain", "");
                res.addHeader("Content-Range", "bytes */" + fileLen);
                res.addHeader("ETag", etag);
            } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else {
                res = this.newFixedFileResponse(file, mime);
                res.addHeader("Content-Length", "" + fileLen);
                res.addHeader("ETag", etag);
            }
        }
        catch (IOException ioe) {
            res = this.getForbiddenResponse("Reading file failed.");
        }
        return res;
    }

    private NanoHTTPD.Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        NanoHTTPD.Response res = this.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mime, new FileInputStream(file), (int)file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

}

