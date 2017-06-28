/*
 * Decompiled with CFR 0_113.
 */

package sampling.evaluation.blogger;

import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Div;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Document;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.MapTokenResolver;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Script;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Style;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Util;
import sampling.evaluation.blogger.fi.iki.elonen.JavaPluginResponse;
import sampling.evaluation.blogger.fi.iki.elonen.NanoHTTPD;
import sampling.evaluation.blogger.fi.iki.elonen.RedirectException;
import sampling.evaluation.blogger.fi.iki.elonen.RenderingClass;
import sampling.evaluation.blogger.fi.iki.elonen.samples.BlogReader;
import sampling.evaluation.blogger.fi.iki.elonen.samples.BlogWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class Write
implements RenderingClass {
    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws RedirectException {
        BlogWriter blogWriter = new BlogWriter(BlogReader.blogs);
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            try {
                HashMap<String, String> files = new HashMap<String, String>();
                session.parseBody(files);
                String q = session.getQueryParameterString();
                Map<String, String> data = Util.parseQueryString(q);
                blogWriter.add(data.get("title"), data.get("author"), data.get("content"));
            }
            catch (NanoHTTPD.ResponseException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        Document document = new Document("I lost the game");
        MapTokenResolver mtr = new MapTokenResolver();
        mtr.put("name", "Blog");
        mtr.put("navigation", Div.fromStatic("navigation", "templates/navigation.html"));
        mtr.put("blogwrite", blogWriter);
        document.getHead().addUnit(Style.fromStatic("css/bootstrap.css")).addUnit(Style.fromStatic("css/homeStyle.css")).addUnit(Script.fromStatic("js/jquery.js")).addUnit(Script.fromStatic("js/bootstrap.js"));
        document.getBody().addUnit(Div.fromStatic("rootContainer", "templates/write.html", mtr));
        return document.toJavaPluginResponse();
    }
}

