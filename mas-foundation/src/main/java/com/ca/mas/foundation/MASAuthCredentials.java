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

import java.util.List;
import java.util.Map;

/**
 * An interface for including the headers and parameters for a set of authorization credentials.
 */
public interface MASAuthCredentials extends Parcelable {

    String REGISTRATION_TYPE = "registration_type";

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
     * @return the authorization headers
     */
    @Internal
    Map<String, List<String>> getHeaders();

    /**
     * @return the list of parameters sent to the gateway to retrieve tokens
     */
    @Internal
    List<Pair<String,String>> getParams();

    /**
     * @return the credentials type,
     * e.g. authorization code, urn:ietf:params:oauth:grant-type:jwt-bearer, password
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
