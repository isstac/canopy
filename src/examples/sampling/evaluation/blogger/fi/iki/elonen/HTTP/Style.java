/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import sampling.evaluation.blogger.fi.iki.elonen.Static;
import java.io.InputStream;
import java.util.Scanner;

public class Style
extends HTTPUnit {
    private String resource;
    private boolean resIsStatic = false;

    public Style(String resource) {
        this(resource, false);
    }

    private Style(String resourceUrl, boolean resIsStatic) {
        Scanner scanner;
        InputStream res;
        this.resIsStatic = resIsStatic;
        this.resource = resIsStatic ? resourceUrl : ((res = ClassLoader.getSystemResourceAsStream("css/" + resourceUrl)) != null ? ((scanner = new Scanner(res).useDelimiter("\\A")).hasNext() ? scanner.next() : "") : "");
    }

    @Override
    public String toString() {
        if (this.resIsStatic) {
            return "<LINK href=\"" + this.resource + "\" rel=\"stylesheet\" type=\"text/css\"/>";
        }
        return "<style>" + this.resource + "</style>";
    }

    public static HTTPUnit fromStatic(String urlFor) {
        return new Style(Static.urlFor(urlFor), true);
    }
}

