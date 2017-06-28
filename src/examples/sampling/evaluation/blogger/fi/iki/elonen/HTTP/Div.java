/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.io.InputStream;
import java.util.Scanner;

public class Div
extends HTTPUnit {
    private String id;
    private String name;
    private String resource;

    public Div(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Div(String id, String resourceUrl) {
        Scanner scanner;
        this.id = id;
        String name = "static/" + resourceUrl;
        InputStream res = ClassLoader.getSystemResourceAsStream(name);
        this.resource = res != null ? ((scanner = new Scanner(res).useDelimiter("\\A")).hasNext() ? scanner.next() : "") : "";
    }

    private Div(String id, String resourceUrl, ITokenResolver tr) {
        Scanner scanner;
        TokenReplacingReader tokenReplacingReader;
        this.id = id;
        String name = "static/" + resourceUrl;
        InputStream res = ClassLoader.getSystemResourceAsStream(name);
        this.resource = res != null ? ((scanner = new Scanner(tokenReplacingReader = new TokenReplacingReader(res, tr)).useDelimiter("\\A")).hasNext() ? scanner.next() : "") : "";
    }

    @Override
    public String toString() {
        return "<div id=\"" + this.id + "\">" + this.resource + "</div>";
    }

    public static HTTPUnit fromStatic(String id, String urlFor) {
        return new Div(id, urlFor);
    }

    public static HTTPUnit fromStatic(String id, String urlFor, ITokenResolver tr) {
        return new Div(id, urlFor, tr);
    }
}

