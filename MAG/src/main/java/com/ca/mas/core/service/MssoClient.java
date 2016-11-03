/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.ResultReceiver;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.creds.PasswordCredentials;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.request.internal.AuthenticateRequest;
import com.ca.mas.core.util.Functions;

/**
 * Encapsulates use of the MssoService.
 */
public class MssoClient {

    private final static String TAG = MssoClient.class.getCanonicalName();
    private final Context sysContext;
    private final MssoContext mssoContext;

    /**
     * Create a service client with the specified MSSO context and system context.
     *
     * @param mssoContext the configured and initialized MSSO context.  Required.
     * @param sysContext  the system context to use for invoking the MssoService.  Required.
     */
    public MssoClient(MssoContext mssoContext, Context sysContext) {
        if (sysContext == null)
            throw new NullPointerException("sysContext");
        if (mssoContext == null)
            throw new NullPointerException("mssoContext");
        this.mssoContext = mssoContext;
        this.sysContext = sysContext;
    }

    /**
     * Submit a request to be processed.
     * <p/>
     * The response to the request will be delivered to the specified result receiver.
     *
     * @param request        the request to send.  Required.
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error.  Required.
     * @return the request ID, which can be used to pick up the response once it is available.
     */
    public long processRequest(MAGRequest request, ResultReceiver resultReceiver) {
        MssoRequest mssoRequest = new MssoRequest(this, mssoContext, request, resultReceiver);
        MssoRequestQueue.getInstance().addRequest(mssoRequest);

        final long requestId = mssoRequest.getId();
        Intent intent = new Intent(MssoIntents.ACTION_PROCESS_REQUEST, null, sysContext, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        sysContext.startService(intent);
        return requestId;
    }

    /**
     * Logs in a user with a username and password. The existing user session will be logout and login with the provided username
     * and password.
     * <p/>
     * <p>The response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread</p>
     *
     * @param username       The username to log in with
     * @param password       The password to log in with
     * @param resultReceiver The resultReceiver to notify when a response is available, or if there is an error. Required.
     */
    public void authenticate(final String username, final char[] password, final MAGResultReceiver resultReceiver) {

        final MssoRequest mssoRequest = new MssoRequest(this, mssoContext, new AuthenticateRequest(), resultReceiver);
        MssoRequestQueue.getInstance().addRequest(mssoRequest);
        long requestId = mssoRequest.getId();

        final Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, sysContext, MssoService.class);
        Credentials credentials = new PasswordCredentials(username, password);
        intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, credentials);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mssoContext.logout(true);
                } catch (Exception e) {
                    resultReceiver.onError(new MAGError(e));
                    return null;
                }
                sysContext.startService(intent);
                return null;
            }
        }.execute((Void) null);
    }

    /**
     * Submit a wakeup message to the intent service, ensuring that any enqueued requests are being processed.
     */
    public void processPendingRequests() {
        // Currently this should only be necessary when we have started the UNLOCK activity.
        // For the Log On activity, it should take care of signalling the MssoService when it should retry.
        if (MssoState.isExpectedUnlock()) {
            Intent intent = new Intent(MssoIntents.ACTION_PROCESS_REQUEST, null, sysContext, MssoService.class);
            intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, (long) -1);
            sysContext.startService(intent);
        }
    }

    /**
     * Collect a response to the specified request ID, if one is available.
     * <p/>
     * This can be called after the result receiver is notified about the response.
     * <p/>
     * Once a response has been picked up, it is removed from the queue and cannot be picked up a second time.
     *
     * @param requestId the request ID whose response to pick up.
     * @return the response, or null if one is not available at this time.
     */
    public static MAGResponse takeMAGResponse(long requestId) {
        return MssoResponseQueue.getInstance().takeResponse(requestId).getHttpResponse();
    }

    /**
     * Cancel the specified request ID.  If the response notification has not already been delivered (or is not already in progress)
     * by the time this method executes, a response notification will never occur fo the specified request ID.
     *
     * @param requestId the request ID to cancel.
     */
    public void cancelRequest(long requestId) {
        MssoRequestQueue.getInstance().takeRequest(requestId);
        MssoResponseQueue.getInstance().takeResponse(requestId);
        Intent intent = new Intent(MssoIntents.ACTION_CANCEL_REQUEST, null, sysContext, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        sysContext.startService(intent);
    }

    /**
     * Canceling any pending requests and responses that were created by this MssoClient.
     */
    public void cancelAll() {
        MssoRequestQueue.getInstance().removeMatching(new Functions.Unary<Boolean, MssoRequest>() {
            @Override
            public Boolean call(MssoRequest mssoRequest) {
                return mssoRequest.getCreator() == MssoClient.this;
            }
        });
        MssoResponseQueue.getInstance().removeMatching(new Functions.Unary<Boolean, MssoResponse>() {
            @Override
            public Boolean call(MssoResponse mssoResponse) {
                return mssoResponse.getRequest().getCreator() == MssoClient.this;
            }
        });
    }
}
