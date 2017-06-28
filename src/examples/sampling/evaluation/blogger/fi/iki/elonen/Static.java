/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;

public class Static
implements RenderingClass {
    private Logger LOG = Logger.getLogger(Static.class.getName());

    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws FileNotFoundException {
        String resourceName = this.getResourceName(session);
        InputStream res = ClassLoader.getSystemResourceAsStream("static/" + resourceName);
        if (res != null) {
            Scanner scanner = new Scanner(res).useDelimiter("\\A");
            String resource = scanner.hasNext() ? scanner.next() : "";
            this.LOG.info("Loading static/" + resourceName);
            switch (resourceName.substring(resourceName.lastIndexOf(46), resourceName.length())) {
                case ".css": {
                    return new JavaPluginResponse(resource, "text/css");
                }
                case ".js": {
                    return new JavaPluginResponse(resource, "application/javascript");
                }
                case ".ico": {
                    return new JavaPluginResponse(resource, "image/x-icon");
                }
                case ".png": {
                    return new JavaPluginResponse(resource, "image/png");
                }
            }
            throw new RuntimeException("Invalid Mime extension");
        }
        this.LOG.warning("404 - static/" + resourceName);
        throw new FileNotFoundException("static/" + resourceName);
    }

    private String getResourceName(NanoHTTPD.IHTTPSession session) {
        String q = session.getQueryParameterString();
        if (q != null) {
            for (String s : q.split("&")) {
                String[] split = s.split("=");
                if (split.length <= 1 || !split[0].equals("q")) continue;
                return split[1];
            }
        } else {
            throw new RuntimeException("Query string is null");
        }
        throw new RuntimeException("Static could not be loaded");
    }

    public static String urlFor(String resourcePath) {
        String aClass = Static.class.getCanonicalName();
        return "/" + aClass + "?q=" + resourcePath;
    }
}

