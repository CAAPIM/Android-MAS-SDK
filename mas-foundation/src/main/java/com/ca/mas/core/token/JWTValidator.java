package com.ca.mas.core.token;

public interface JWTValidator {

    public boolean validate(IdToken token) throws JWTInvalidSignatureException, JWTValidationException;
}
