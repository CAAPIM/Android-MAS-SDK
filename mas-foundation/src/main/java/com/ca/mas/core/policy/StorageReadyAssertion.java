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
import com.ca.mas.core.policy.exceptions.TokenStoreUnavailableException;
import com.ca.mas.foundation.MASResponse;

class StorageReadyAssertion implements MssoAssertion {

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        //do nothing
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws TokenStoreUnavailableException {

        // Ensure token store is available
        if (!mssoContext.getTokenManager().isTokenStoreReady())
            throw new TokenStoreUnavailableException();

    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) {
        //do nothing
    }

    @Override
    public void close() {
        //do nothing
    }
}
