package com.ca.mas.core.http;

import com.ca.mas.core.util.KeyUtils;

import java.security.PrivateKey;

public class JwtSignWithFingerprintRequest extends JwtSignRequest {

    public JwtSignWithFingerprintRequest(MAGRequest request) {
        super(request);
    }

    @Override
    protected PrivateKey getPrivateKey() {
        try {
            return KeyUtils.getRsaPrivateKey("SecureAPI");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
