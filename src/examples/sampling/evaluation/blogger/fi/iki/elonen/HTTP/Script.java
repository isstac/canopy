/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import sampling.evaluation.blogger.fi.iki.elonen.Static;
import java.io.InputStream;
import java.util.Scanner;

public class Script
extends HTTPUnit {
    private String resource;
    private boolean resIsStatic = false;

    public Script(String resource) {
        this(resource, false);
    }

    private Script(String resourceUrl, boolean resIsStatic) {
        Scanner scanner;
        InputStream res;
        this.resIsStatic = resIsStatic;
        this.resource = resIsStatic ? resourceUrl : ((res = ClassLoader.getSystemResourceAsStream("scripts/" + resourceUrl)) != null ? ((scanner = new Scanner(res).useDelimiter("\\A")).hasNext() ? scanner.next() : "") : "alert(\"Server failed to load " + resourceUrl + "\");");
    }

    @Override
    public String toString() {
        if (this.resIsStatic) {
            return "<script src=\"" + this.resource + "\"></script>";
        }
        return "<script>" + this.resource + "</script>";
    }

    public static HTTPUnit fromStatic(String urlFor) {
        return new Script(Static.urlFor(urlFor), true);
    }
}

