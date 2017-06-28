/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.util.Date;
import java.util.LinkedHashMap;

public class LoginManager {
    private static LinkedHashMap<Integer, UserLogin> usermap = new LinkedHashMap(16, 0.75f, true);
    private static int loginLength = 1209600;

    public static void setLoginLength(int seconds) {
        loginLength = seconds;
    }

    public static int getLoginPeriod() {
        return loginLength;
    }

    public static synchronized UserLogin getUser(int id) {
        return usermap.get(id);
    }

    public static synchronized int newUser() {
        UserLogin user;
        int i;
        for (i = 0; i < usermap.keySet().size() && (user = usermap.get(i)) != null; ++i) {
            if (user.initiation.getTime() + (long)loginLength >= new Date().getTime()) continue;
            usermap.remove(i);
            break;
        }
        usermap.put(i, new UserLogin());
        return i;
    }

    public static synchronized void terminate(int id) {
        usermap.remove(id);
    }

    public static synchronized boolean hasUser(int userId) {
        return usermap.containsKey(userId);
    }
}

