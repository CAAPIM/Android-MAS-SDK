package com.ca.mas.core.policy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.TokenStoreUnavailableException;

class StorageReadyAssertion implements MssoAssertion {

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {

    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws TokenStoreUnavailableException {

        // Ensure token store is available
        if (!mssoContext.getTokenManager().isTokenStoreReady())
            throw new TokenStoreUnavailableException();

    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) {

    }

    @Override
    public void close() {

    }
}
