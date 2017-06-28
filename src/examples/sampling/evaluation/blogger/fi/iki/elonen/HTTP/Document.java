/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import sampling.evaluation.blogger.fi.iki.elonen.JavaPluginResponse;

public class Document {
    private String doctype = "html";
    private Head head;
    private Body body;

    public String getDoctype() {
        return this.doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public Document(String title) {
        this.head = new Head(title);
        this.body = new Body();
    }

    public Head getHead() {
        return this.head;
    }

    public Body getBody() {
        return this.body;
    }

    public String toString() {
        return "<!DOCTYPE " + this.doctype + ">\n" + "<html>\n" + this.head.toString() + this.body.toString() + "</html>\n";
    }

    public JavaPluginResponse toJavaPluginResponse() {
        return new JavaPluginResponse(this.toString(), "text/html");
    }
}

