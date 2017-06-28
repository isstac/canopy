/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.stac.example;

import sampling.evaluation.blogger.fi.iki.elonen.JavaPluginResponse;
import sampling.evaluation.blogger.fi.iki.elonen.NanoHTTPD;
import sampling.evaluation.blogger.fi.iki.elonen.RenderingClass;
import java.io.IOException;
import java.util.TreeMap;

public class Next
implements RenderingClass {
    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) {
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            TreeMap<String, String> files = new TreeMap<String, String>();
            try {
                session.parseBody(files);
            }
            catch (NanoHTTPD.ResponseException | IOException e) {
                throw new RuntimeException(e);
            }
            String input = files.get("postData");
            if (input.equals("Initial")) {
                return new JavaPluginResponse("You", "text/plain");
            }
            if (input.equals("You")) {
                return new JavaPluginResponse("Lost", "text/plain");
            }
            if (input.equals("Lost")) {
                return new JavaPluginResponse("The", "text/plain");
            }
            if (input.equals("The")) {
                return new JavaPluginResponse("Game", "text/plain");
            }
            return new JavaPluginResponse("Halt", "text/plain");
        }
        throw new RuntimeException("Something is broken");
    }
}

