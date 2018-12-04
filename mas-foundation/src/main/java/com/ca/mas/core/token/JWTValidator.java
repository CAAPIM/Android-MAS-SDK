package com.ca.mas.core.token;

import com.ca.mas.core.context.MssoContext;

public interface JWTValidator {

    public boolean validate(MssoContext context,IdToken token) throws JWTInvalidSignatureException, JWTValidationException;
}
