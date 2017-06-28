/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import sampling.evaluation.blogger.fi.iki.elonen.HTTP.Div;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.HTTPUnit;
import sampling.evaluation.blogger.fi.iki.elonen.HTTP.MapTokenResolver;

public class Navigator
extends HTTPUnit {
    UserLogin login;

    public Navigator(int userId) {
        this.login = LoginManager.getUser(userId);
    }

    @Override
    public String toString() {
        MapTokenResolver mtr = new MapTokenResolver();
        mtr.put("loggedIn", String.valueOf(this.login != null && this.login.authenticated));
        mtr.put("userName", String.valueOf(this.login.userName));
        HTTPUnit navigator = Div.fromStatic("navigator", "templates/navigator.html", mtr);
        return navigator.toString();
    }
}

