/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaWebServerPlugin
implements WebServerPlugin {
    private static final Logger LOG = Logger.getLogger(JavaWebServerPlugin.class.getName());

    @Override
    public boolean canServeUri(String uri, File rootDir) {
        return (uri = uri.substring(1).replaceAll("/", ".")).isEmpty() || new URIVerifier().verify(uri);
    }

    @Override
    public void initialize(Map<String, String> commandLineOptions) {
    }

    @Override
    public NanoHTTPD.Response serveFile(String uri, Map<String, String> headers, NanoHTTPD.IHTTPSession session, File file, String mimeType) {
        RenderingClass renderingClass;
        JavaPluginResponse render;
        uri = uri.equals("/") ? "Index" : uri.substring(1).replaceAll("/", ".");
        try {
            Class<?>[] interfaces;
            boolean should404 = true;
            Class aClass = Class.forName(uri);
            for (Class anInterface : interfaces = aClass.getInterfaces()) {
                if (!anInterface.getSimpleName().equals("RenderingClass")) continue;
                should404 = false;
            }
            if (should404) {
                throw new ClassNotFoundException();
            }
            renderingClass = (RenderingClass)aClass.newInstance();
            LOG.log(Level.INFO, session.getMethod().toString() + ": " + uri);
        }
        catch (IllegalAccessException | InstantiationException e) {
            String msg = e.getClass().getName() + " Detected in Java Plugin";
            LOG.log(Level.SEVERE, msg, e);
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/html", new ByteArrayInputStream(msg.getBytes()), msg.length());
        }
        catch (ClassCastException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "404 - Not Found: " + session.getUri());
            String msg = "404 - /" + uri + " Not Found";
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", new ByteArrayInputStream(msg.getBytes()), msg.length());
        }
        try {
            render = renderingClass.render(session);
        }
        catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "404 - Not Found: " + session.getUri());
            String msg = "404 - /" + uri + " Not Found";
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", new ByteArrayInputStream(msg.getBytes()), msg.length());
        }
        catch (RedirectException e) {
            NanoHTTPD.Response response = new NanoHTTPD.Response(LocalStatus.FOUND, "text/plain", null, 0);
            response.addHeader("Location", e.to);
            return response;
        }
        byte[] s = render.getBytes();
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, render.getContentType(), new ByteArrayInputStream(s), s.length);
    }

    static enum LocalStatus implements NanoHTTPD.Response.IStatus
    {
        FOUND(302, "Found");
        
        private String desc;
        private int code;

        private LocalStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        @Override
        public String getDescription() {
            return this.desc;
        }

        @Override
        public int getRequestStatus() {
            return this.code;
        }
    }

}

