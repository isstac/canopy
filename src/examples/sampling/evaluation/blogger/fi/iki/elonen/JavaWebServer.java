/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaWebServer
extends NanoHTTPD {
    public static final String INTERNAL_SERVER_ERROR = "500 - Internal Server Error";
    public static final String METHOD_NOT_ALLOWED = "405 - Method Not Allowed";
    public static final String NOT_FOUND = "404 - Not Found";
    private Logger LOG = Logger.getLogger(JavaWebServer.class.getName());
    private WebServerPlugin plugin = new JavaWebServerPluginInfo().getWebServerPlugin("text/html");

    public static void main(String[] args) {
        ServerRunner.executeInstance(new JavaWebServer(null, 8080));
    }

    public JavaWebServer(int port) {
        super(port);
    }

    public JavaWebServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        if (this.plugin.canServeUri(session.getUri(), null)) {
            switch (session.getMethod()) {
                case GET: 
                case POST: {
                    try {
                        return this.plugin.serveFile(session.getUri(), session.getHeaders(), session, null, "text/html");
                    }
                    catch (Exception e) {
                        this.LOG.log(Level.SEVERE, "500 - Internal Server Error", e);
                        return this.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/html", "500 - Internal Server Error");
                    }
                }
            }
            this.LOG.log(Level.SEVERE, "405 - Method Not Allowed");
            return this.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "text/plain", "405 - Method Not Allowed");
        }
        this.LOG.log(Level.WARNING, "404 - Not Found: " + session.getUri());
        return this.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "404 - Not Found: " + session.getUri());
    }

}

