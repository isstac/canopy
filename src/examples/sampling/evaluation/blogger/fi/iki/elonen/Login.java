/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Login
implements RenderingClass {
    public static boolean doNotDrop;

    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws FileNotFoundException, RedirectException {
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            int userId;
            TreeMap<String, String> files = new TreeMap<String, String>();
            try {
                session.parseBody(files);
            }
            catch (NanoHTTPD.ResponseException | IOException e) {
                throw new RuntimeException(e);
            }
            NanoHTTPD.CookieHandler cookies = session.getCookies();
            String userIdString = cookies.read("userId");
            if (userIdString == null) {
                userId = LoginManager.newUser();
            } else {
                userId = Integer.valueOf(userIdString);
                if (!LoginManager.hasUser(userId)) {
                    userId = LoginManager.newUser();
                }
            }
            Map<String, String> parms = session.getParms();
            if (!(parms.containsKey("username") && parms.containsKey("password") && CredentialManager.insecureCheckPasswd(parms.get("username"), parms.get("password")))) {
                return new JavaPluginResponse("{ \"message\": \"Invalid Username or Password\" }", "application/json");
            }
            cookies.set("userId", String.valueOf(userId), LoginManager.getLoginPeriod());
            UserLogin user = LoginManager.getUser(userId);
            user.authenticated = true;
            user.userName = parms.get("username");
            return new JavaPluginResponse("{\"message\": \"Logged In\"}", "application/json");
        }
        throw new RuntimeException("You can't make that request here.");
    }
}

