/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import android.os.Bundle;
import android.os.ResultReceiver;

import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.foundation.MASRequest;

import org.json.JSONObject;

/**
 * <p>Top-level interface for the Mobile SSO SDK.</p>
 * Use {@link MobileSsoFactory} to obtain an implementation of this.
 */
public interface MobileSso {

    /**
     * Submit a API request to be processed asynchronously.
     *
     * <ul>
     * <li>The response to the request will eventually be delivered to the specified result receiver.</li>
     * <li>This method returns immediately to the calling thread.</li>
     * <li>An activity may be started if a device lock code needs to be configured or if the user must be prompted for a username and password.</li>
     * </ul>
     *
     * @param request        the request to send.  Required.
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error.  Required.
     *                       The result code is defined under {@link com.ca.mas.core.service.MssoIntents} RESULT_CODE_*,
     *                       To retrieve the error message from the returned Bundle with key
     *                       {@link com.ca.mas.core.service.MssoIntents#RESULT_ERROR_MESSAGE}.
     *                       <p>
     *                       A helper class {@link MAGResultReceiver} defined a standard interface to capture the result
     *                       of the API request.
     *                       </p>
     * @return the request ID, which can be used to cancel the request, to cancel the request please refer to
     * {@link #cancelRequest(long, Bundle)}}
     */

    long processRequest(MASRequest request, ResultReceiver resultReceiver);

    /**
     * <p>Authenticates a user with a MASAuthCredentials object.</p>
     *
     * <p>The response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread</p>
     *
     * @param credentials the credentials to authenticate with
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error. Required.
     */
    void authenticate(MASAuthCredentials credentials, MAGResultReceiver<JSONObject> resultReceiver);

    /**
     * <p>Requests that any pending queued requests be processed.</p>
     * <p>This can be called from an activity's onResume() method to ensure that
     * any pending requests waiting for an initial unlock code on the device get a chance to continue.</p>
     * <p>This method returns immediately to the calling thread.</p>
     * <p>An activity may be started if a device lock code (still) needs to be configured
     * or if the user must be prompted for a username and password.</p>
     */
    void processPendingRequests();

    /**
     * Cancels the specified request ID. If the response notification has not already been delivered
     * by the time this method executes, a response notification will never occur for the specified request ID.
     *
     * @param requestId the request ID to cancel.
     * @param data the data to the cancelled request {@link MAGResultReceiver#onRequestCancelled(Bundle)}
     */
    void cancelRequest(long requestId, Bundle data);

    /**
     * Cancels all requests. If the response notification has not already been delivered
     * by the time this method executes, response notification will never occur.
     *
     * @param data the data to the all the cancelled request {@link MAGResultReceiver#onRequestCancelled(Bundle)}
     */
    void cancelAllRequests(Bundle data);

    /**
     * <p>Clear all tokens in the shared token store.</p>
     * <b>NOTE: You should not normally use this method.</b>
     * This method destroys the client private key, effectively un-registering the device, and should only be used
     * for testing or recovery purposes.
     * <p>If you just wish to log out the current SSO user see the {@link #logout} method instead.</p>
     */
    void destroyAllPersistentTokens();

    /**
     * <p>Remove this device registration from the server.  The token server will identify the device making the request
     * by its TLS client certificate.</p>
     * <p>This does not affect the local cached access token,
     * cached username and password, or the shared token storage in any way.  The client will continue to attempt
     * to present its TLS client certificate on future calls to the token server or a web API endpoint.</p>
     * To destroy the client-side record of the device registration, call {@link #destroyAllPersistentTokens()}.
     * <p>The communication with the token server will occur on the current thread.  As this may take some time,
     * callers running on the UI thread should consider running this method within an AsyncTask.</p>
     *
     * @throws com.ca.mas.core.context.MssoException if there is an error while attempting to tell the token server to unregister this device.
     */
    void removeDeviceRegistration();

    /**
     * Check if the user has already been logged in.
     *
     * @return true if the id token has been acquired and cached, false if the id token is not available
     */
    boolean isLogin();

    /**
     * Checks if the device has already been registered.
     *
     * @return true if device registered has already completed and a client cert chain and ID token are present in the token store.
     * false if registration is required.
     */
    boolean isDeviceRegistered();

    /**
     * Performs a remote authorization with the provider URL. (For Example QRCode)
     *
     * @param url            The temporary URL to enable the remote session.
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error.  Required.
     */
    void authorize(String url, ResultReceiver resultReceiver);

    /**
     * Retrieves the Authentication Providers from the server.
     * Authentication providers will not be retrieved if the user is already authenticated.
     */
    AuthenticationProvider getAuthenticationProvider() throws Exception;

}
