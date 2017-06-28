/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.util.LinkedList;

public class Body
extends HTTPUnit {
    private LinkedList<HTTPUnit> units = new LinkedList();

    public Body addUnit(HTTPUnit unit) {
        this.units.add(unit);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("<body>");
        for (HTTPUnit unit : this.units) {
            sb.append(unit);
        }
        sb.append("</body>");
        return sb.toString();
    }
}

