/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.samples;

import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Div;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.HTTPUnit;

import java.util.ArrayList;

public class BlogWriter
extends HTTPUnit {
    private final ArrayList<Blog> blogs;

    public BlogWriter(ArrayList<Blog> blogs) {
        this.blogs = blogs;
    }

    @Override
    public String toString() {
        return Div.fromStatic("writer", "templates/blogwrite.html").toString();
    }

    public void add(String title, String author, String content) {
        this.blogs.add(new Blog(title, author, content));
    }
}

