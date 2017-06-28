/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

public class JavaPluginResponse {
    private String contentType;
    private String response;

    public JavaPluginResponse(String response, String contentType) {
        this.response = response;
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public byte[] getBytes() {
        return this.response.getBytes();
    }
}

