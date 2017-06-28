/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

public class JavaWebServerPluginInfo
implements WebServerPluginInfo {
    @Override
    public String[] getIndexFilesForMimeType(String mime) {
        return new String[]{"Index"};
    }

    @Override
    public String[] getMimeTypes() {
        return new String[]{"text/html"};
    }

    @Override
    public WebServerPlugin getWebServerPlugin(String mimeType) {
        return new JavaWebServerPlugin();
    }
}

