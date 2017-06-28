/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.File;
import java.util.Map;

public interface WebServerPlugin {
    public boolean canServeUri(String var1, File var2);

    public void initialize(Map<String, String> var1);

    public NanoHTTPD.Response serveFile(String var1, Map<String, String> var2, NanoHTTPD.IHTTPSession var3, File var4, String var5);
}

