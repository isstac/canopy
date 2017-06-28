/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

public class RedirectException
extends Exception {
    public final String to;

    public RedirectException(String message, String to) {
        super(message);
        this.to = to;
    }
}

