/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.policy;

import android.content.Context;
import androidx.annotation.NonNull;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.oauth.OAuthClientUtil;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

class SecureLockAssertion implements MssoAssertion {
    private TokenManager tokenManager;

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        tokenManager = mssoContext.getTokenManager();
        if (mssoContext.getConfigurationProvider() == null) {
            throw new NullPointerException("ConfigurationProvider is null");
        }
    }

    /**
     * If a request to a protected endpoint is made with a locked session, we stop the request.
     * @param mssoContext The MSSO context. Required.
     * @param request     The pending HTTP request. Required.
     * @throws SecureLockException
     */
    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) {
        byte[] secureToken = tokenManager.getSecureIdToken();
        if (secureToken != null) {
            //Clear the access tokens, the session may be locked by other App.
            MASRequest revokeRequest = OAuthClientUtil.getRevokeRequest();
            if (revokeRequest != null) {
                MAS.invoke(OAuthClientUtil.getRevokeRequest(), null);
            }
            mssoContext.clearAccessAndRefreshTokens();
            throw new SecureLockException("The session is currently locked.");
        }
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) {
        //do Nothing
    }

    @Override
    public void close() {
        //do Nothing
    }
}
