/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CredentialManager {
    private static HashMap<String, String> passwd = new HashMap();
    private static Lock passwdLock = new ReentrantLock();

    public static boolean insecureCheckPasswd(String username, String passwdExt) {
        passwdLock.lock();
        String passwdInt = passwd.get(username);
        passwdLock.unlock();
        return passwdInt != null && passwdInt.hashCode() == passwdExt.hashCode();
    }

    static {
        passwd.put("guest", "guest");
        passwd.put("admin", "admin");
        passwd.put("root", "root");
        passwd.put("Administrator", "Administrator");
        boolean bl = Login.doNotDrop;
    }
}

