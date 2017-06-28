/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

public class Raw
extends HTTPUnit {
    private String rawData;

    public Raw(String data) {
        this.rawData = data;
    }

    @Override
    public String toString() {
        return this.rawData;
    }
}

