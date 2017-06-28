/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.util.Map;
import java.util.TreeMap;

public class Util {
    public static Map<String, String> parseQueryString(String q) {
        TreeMap<String, String> m = new TreeMap<String, String>();
        if (q != null) {
            for (String s : q.split("&")) {
                String[] split = s.split("=");
                if (split.length <= 1) continue;
                m.put(split[0], split[1]);
            }
        }
        return m;
    }
}

