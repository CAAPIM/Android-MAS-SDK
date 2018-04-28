/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;

/**
 * Interface that will receive various notifications and requests for the MAG client.
 */
public interface MobileSsoListener {
    /**
     * Notifies the host application that a request to authenticate is triggered by the MAG authentication process.
     *
     * @param requestId
     * @param authenticationProvider
     */
    void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider);

    /**
     * @deprecated
     */
    void onOtpAuthenticationRequest(MASOtpAuthenticationHandler otpAuthenticationHandler);

}
