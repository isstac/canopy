/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.util.LinkedList;

public class Head
extends HTTPUnit {
    private LinkedList<HTTPUnit> units = new LinkedList();
    private String title;

    public Head(String title) {
        this.title = title;
    }

    public Head addUnit(HTTPUnit unit) {
        this.units.add(unit);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("<head>").append("<title>").append(this.title).append("</title>\n");
        for (HTTPUnit unit : this.units) {
            sb.append(unit);
        }
        sb.append("</head>");
        return sb.toString();
    }
}

