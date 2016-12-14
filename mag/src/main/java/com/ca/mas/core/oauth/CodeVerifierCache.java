package com.ca.mas.core.oauth;

import java.util.Map;

/**
 * Temporary cache to store code verifier
 */
public class CodeVerifierCache {

    private static CodeVerifierCache instance = new CodeVerifierCache();

    private String state;
    private String codeVerifier;

    private CodeVerifierCache() {
    }

    public static CodeVerifierCache getInstance() {
        return instance;
    }

    public String getCurrentCodeVerifier() {
        return codeVerifier;
    }

    public void store(String state, String codeVerifier) {
        this.state = state;
        this.codeVerifier = codeVerifier;
    }

    public String take(String state) {
        if (this.state == null && state != null
                || this.state != null && !this.state.equals(state)) {
            throw new IllegalStateException("OAuth State Mismatch");
        }
        String cv = this.codeVerifier;
        this.state = null;
        this.codeVerifier = null;
        return cv;
    }

}
