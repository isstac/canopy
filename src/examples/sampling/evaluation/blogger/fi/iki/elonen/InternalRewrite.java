/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.ByteArrayInputStream;
import java.util.Map;

public class InternalRewrite
extends NanoHTTPD.Response {
    private final String uri;
    private final Map<String, String> headers;

    public InternalRewrite(Map<String, String> headers, String uri) {
        super(NanoHTTPD.Response.Status.OK, "text/html", new ByteArrayInputStream(new byte[0]), 0);
        this.headers = headers;
        this.uri = uri;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getUri() {
        return this.uri;
    }
}

