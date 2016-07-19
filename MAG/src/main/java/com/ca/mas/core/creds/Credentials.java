/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.creds;

import android.os.Parcelable;
import android.util.Pair;

import com.ca.mas.core.context.MssoContext;

import java.util.List;
import java.util.Map;

public interface Credentials extends Parcelable {

    /**
     * Clear the credentials
     */
    void clear();

    /**
     * Validate the credentials.
     *
     * @return True if the credentials is valid, false for invalid
     */
    boolean isValid();

    /**
     * Return the authorization header that send to the gateway.
     *
     * @param context The Msso Context
     * @return The authorization header.
     */
    Map<String, List<String>> getHeaders(MssoContext context);

    /**
     * Return the list of params that send to the gateway to retrieve token.
     *
     * @param context The Msso Context
     * @return
     */
    List<Pair<String,String>> getParams(MssoContext context);

    /**
     * Return the grant type for this credentials. For example password, , authorization_code.
     *
     * @return
     */
    String getGrantType();

    /**
     * Return the username for this credentials.
     * @return
     */
    String getUsername();

    /**
     * Return true if the credential can be reuse. Otherwise false.
     * @return
     */
    boolean isReuseable();

}
