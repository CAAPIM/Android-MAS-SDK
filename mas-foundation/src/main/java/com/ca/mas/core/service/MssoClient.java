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
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.request.internal.AuthenticateRequest;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.foundation.MASAuthCredentialsPassword;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Encapsulates use of the MssoService.
 */
public class MssoClient {
    private final Context appContext;
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
        this.appContext = sysContext.getApplicationContext();
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
    public long processRequest(MASRequest request, ResultReceiver resultReceiver) {
        MssoRequest mssoRequest = new MssoRequest(this, mssoContext, request, resultReceiver);
        MssoRequestQueue.getInstance().addRequest(mssoRequest);

        final long requestId = mssoRequest.getId();
        Context context = appContext;
        Intent intent = new Intent(MssoIntents.ACTION_PROCESS_REQUEST, null, context, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        context.startService(intent);
        return requestId;
    }

    private Intent createAuthenticationIntent(MASAuthCredentials credentials, MAGResultReceiver resultReceiver) {
        final MssoRequest mssoRequest = new MssoRequest(this, mssoContext, new AuthenticateRequest(), resultReceiver);
        MssoRequestQueue.getInstance().addRequest(mssoRequest);
        long requestId = mssoRequest.getId();

        Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, appContext, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, credentials);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        return intent;
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
        if (username == null || password == null) {
            throw new NullPointerException("Username or password cannot be null");
        }

        MASAuthCredentials credentials = new MASAuthCredentialsPassword(username, password);
        Intent intent = createAuthenticationIntent(credentials, resultReceiver);
        new MssoClientLogoutAsyncTask(appContext, mssoContext, resultReceiver, intent).execute((Void) null);
    }

    private static class MssoClientLogoutAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context appContext;
        private MAGResultReceiver aResultReceiver;
        private Intent aIntent;
        private MssoContext aMssoContext;

        MssoClientLogoutAsyncTask(Context context, MssoContext mssoContext, MAGResultReceiver resultReceiver, Intent intent) {
            appContext = context;
            aResultReceiver = resultReceiver;
            aMssoContext = mssoContext;
            aIntent = intent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                aMssoContext.logout(true);
            } catch (SecureLockException e) {
                if (aResultReceiver != null) {
                    aResultReceiver.onError(new MAGError(e));
                }
            } catch (Exception ignore) {
                if (DEBUG) Log.w(TAG, ignore);
            }

            Context context = appContext;
            context.startService(aIntent);
            return null;
        }
    }

    /**
     * <p>Logs in a user with MASAuthCredentials.
     * <p>The response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread</p>
     *
     * @param credentials       The credentials to log in with
     * @param resultReceiver The resultReceiver to notify when a response is available, or if there is an error. Required.
     */
    public void authenticate(final MASAuthCredentials credentials, final MAGResultReceiver resultReceiver) {
        Intent intent = createAuthenticationIntent(credentials, resultReceiver);
        new MssoClientAuthenticateAsyncTask(appContext, mssoContext, resultReceiver, intent).execute((Void) null);
    }

    private static class MssoClientAuthenticateAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context aAppContext;
        private MAGResultReceiver aResultReceiver;
        private Intent aIntent;
        private MssoContext aMssoContext;

        MssoClientAuthenticateAsyncTask(Context context, MssoContext mssoContext, MAGResultReceiver resultReceiver, Intent intent) {
            aAppContext = context;
            aResultReceiver = resultReceiver;
            aMssoContext = mssoContext;
            aIntent = intent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                aMssoContext.logout(true);
            } catch (SecureLockException e) {
                if (aResultReceiver != null) {
                    aResultReceiver.onError(new MAGError(e));
                }
            } catch (Exception ignore) {
                if (DEBUG) Log.w(TAG, ignore);
            }
            aAppContext.startService(aIntent);
            return null;
        }
    }

    /**
     * Submit a wakeup message to the intent service, ensuring that any enqueued requests are being processed.
     */
    public void processPendingRequests() {
        // Currently this should only be necessary when we have started the UNLOCK activity.
        // For the Log On activity, it should take care of signalling the MssoService when it should retry.
            Intent intent = new Intent(MssoIntents.ACTION_PROCESS_REQUEST, null, appContext, MssoService.class);
            intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, (long) -1);
            appContext.startService(intent);
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
    public static MASResponse takeMAGResponse(long requestId) {
        return MssoResponseQueue.getInstance().takeResponse(requestId).getHttpResponse();
    }

    /**
     * Cancel the specified request ID.  If the response notification has not already been delivered (or is not already in progress)
     * by the time this method executes, a response notification will never occur for the specified request ID.
     *
     * @param requestId the request ID to cancel.
     */
    public void cancelRequest(long requestId, Bundle data) {
        MssoRequest request = null;
        MssoResponseQueue.getInstance().takeResponse(requestId);
        request = MssoRequestQueue.getInstance().takeRequest(requestId);
        if (request == null) {
            request = MssoActiveQueue.getInstance().takeRequest(requestId);
        }
        if (request != null && request.getResultReceiver() != null) {
            request.getResultReceiver().send(MssoIntents.RESULT_CODE_ERR_CANCELED, data);
        }
    }

    /**
     * Canceling any pending requests and responses that were created by this MssoClient.
     */
    public void cancelAll(Bundle data) {
        MssoRequestQueue.getInstance().removeMatching(new Functions.Unary<Boolean, MssoRequest>() {
            @Override
            public Boolean call(MssoRequest mssoRequest) {
                return mssoRequest.getCreator() == MssoClient.this;
            }
        }, data);
        MssoResponseQueue.getInstance().removeMatching(new Functions.Unary<Boolean, MssoResponse>() {
            @Override
            public Boolean call(MssoResponse mssoResponse) {
                return mssoResponse.getRequest().getCreator() == MssoClient.this;
            }
        }, data);
        MssoActiveQueue.getInstance().removeMatching(new Functions.Unary<Boolean, MssoRequest>() {
            @Override
            public Boolean call(MssoRequest mssoRequest) {
                return mssoRequest.getCreator() == MssoClient.this;
            }
        }, data);
    }
}
