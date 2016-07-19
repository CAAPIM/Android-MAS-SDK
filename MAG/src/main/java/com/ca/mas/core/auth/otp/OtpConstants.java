/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.otp;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;

public class OtpConstants {

    /** An Intent with this action is used to start a OTP dialog activity when OTP is needed.
     * Handled by OTP activity. */
    public static final String ACTION_DISPLAY_OTP_PROTECTED_DATA = "com.ca.mas.core.service.action.DISPLAY_OTP_PROTECTED_DATA";

    public static final String X_OTP = "X-OTP";
    public static final String X_OTP_CHANNEL = "X-OTP-CHANNEL";
    public static final String X_OTP_RETRY = "X-OTP-RETRY";
    public static final String X_OTP_RETRY_INTERVAL = "X-OTP-RETRY-INTERVAL";
    public static final String X_CA_ERR = "x-ca-err";

    public static final String OTP_REQUESTID = "requestID";

    public static final String OTP_AUTH_URL = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.AUTHENTICATE_OTP_PATH);
    public static final String IS_INVALID_OTP = "IS_INVALID_OTP";
}
