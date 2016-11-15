package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.context.MssoContext;

public class InvalidClientCredentialException extends RetryRequestException {

    public InvalidClientCredentialException() {
    }

    public InvalidClientCredentialException(String message) {
        super(message);
    }

    public InvalidClientCredentialException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public void recover(MssoContext context) throws Exception {
        context.clearAccessToken();
        context.clearClientCredentials();
    }
}
