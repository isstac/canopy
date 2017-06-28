/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen.HTTP;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapTokenResolver
implements ITokenResolver,
Map<String, String> {
    protected Map<String, String> tokenMap = null;

    public MapTokenResolver(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    public MapTokenResolver() {
        this.tokenMap = new HashMap<String, String>();
    }

    @Override
    public String resolveToken(String tokenName) {
        return this.tokenMap.get(tokenName);
    }

    @Override
    public int size() {
        return this.tokenMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.tokenMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.tokenMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.tokenMap.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return this.tokenMap.get(key);
    }

    @Override
    public String put(String key, String value) {
        return this.tokenMap.put(key, value);
    }

//    @Override
    public String put(String key, HTTPUnit unit) {
        return this.tokenMap.put(key, unit.toString());
    }

    @Override
    public String remove(Object key) {
        return this.tokenMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        this.tokenMap.putAll(m);
    }

    @Override
    public void clear() {
        this.tokenMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.tokenMap.keySet();
    }

    @Override
    public Collection<String> values() {
        return this.tokenMap.values();
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return this.tokenMap.entrySet();
    }
}

