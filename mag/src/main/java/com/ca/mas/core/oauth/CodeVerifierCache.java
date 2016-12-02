package com.ca.mas.core.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Temporary cache to store code verifier
 */
public class CodeVerifierCache {

    private final LinkedHashMap<String, String> cache = new LinkedHashMap<>();

    private static CodeVerifierCache instance = new CodeVerifierCache();

    private CodeVerifierCache() {
    }

    public static CodeVerifierCache getInstance() {
        return instance;
    }

    public void put(String key, String codeVerifier) {
        cache.put(key, codeVerifier);
    }

    public String get(String key) {
        return cache.get(key);
    }


    public String takeAndClear(String key) {
        String c = null;
        if (key == null) {
            Map.Entry<String, String> last = null;
            for (Map.Entry e : cache.entrySet()) last = e;
            if (last != null) {
                c = last.getValue();
            }
        } else {
            c = cache.get(key);
        }
        cache.clear();
        return c;
    }

}
