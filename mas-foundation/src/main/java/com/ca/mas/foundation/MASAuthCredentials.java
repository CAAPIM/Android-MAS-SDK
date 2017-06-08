/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.os.Parcelable;
import android.util.Pair;

import com.ca.mas.core.context.MssoContext;

import java.util.List;
import java.util.Map;

public interface MASAuthCredentials extends Parcelable {

    /**
     * Clear the credentials.
     */
    void clear();

    /**
     * @return true if the credentials are valid
     */
    boolean isValid();

    /**
     * Return the authorization headers that are sent to the gateway.
     *
     * @param context The Msso Context
     * @return the authorization headers
     */
    Map<String, List<String>> getHeaders(MssoContext context);

    /**
     * @param context The Msso Context
     * @return the list of parameters sent to the gateway to retrieve tokens
     */
    List<Pair<String,String>> getParams(MssoContext context);

    /**
     * @return the credentials type, e.g. authorization code, JWT, password
     */
    String getGrantType();

    /**
     * @return true if the credentials can be used to register the device
     */
    boolean canRegisterDevice();

    /**
     * @return the username for these credentials
     */
    String getUsername();

    /**
     * @return true if the credentials can be reused, false otherwise
     */
    boolean isReusable();

}
