/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.ResponseInterceptor;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.oauth.OAuthClient;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.policy.exceptions.CredentialRequiredException;
import com.ca.mas.core.policy.exceptions.TokenStoreUnavailableException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

import java.util.ArrayList;
import java.util.Collection;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * An IntentService that receives outbound HTTP requests encoded into Intents and returns the eventual responses
 * via a ResultReceiver.
 */
public class MssoService extends IntentService {

    public MssoService() {
        super("MssoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            if (DEBUG) Log.w(TAG, "Intent did not contain an action");
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(MssoIntents.EXTRA_REQUEST_ID)) {
            if (DEBUG) Log.w(TAG, "Intent did not contain extras that included a request ID");
            return;
        }

        long requestId = extras.getLong(MssoIntents.EXTRA_REQUEST_ID);
        if (requestId == -1) {
            onProcessAllPendingRequests();
            return;
        }

        MssoRequest request = takeActiveRequest(requestId);
        if (request == null) {
            if (DEBUG)
                Log.d(TAG, "Request ID not found, assuming request is canceled or already processed");
            return;
        }

        if (MssoIntents.ACTION_PROCESS_REQUEST.equals(action)) {
            startThreadedRequest(extras, request);
            return;
        } else if (MssoIntents.ACTION_CREDENTIALS_OBTAINED.equals(action)) {
            //The request is AuthenticateRequest
            onCredentialsObtained(extras, request);
            return;
        }

        if (DEBUG) Log.w(TAG, "Ignoring intent with unrecognized action " + action);
    }

    private void startThreadedRequest(final Bundle extras, final MssoRequest request) {
        //Before assign the request to thread task,
        request.setRunning(true);
        //We don't want to override the extras if it is null.
        if (extras != null) {
            request.setExtra(extras);
        }
        try {
            MssoExecutorService.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    onProcessRequest(request);
                }
            });
        } catch (Exception e) {
            //In case we got rejected to assign a thread to serve the request
            request.setRunning(false);
            throw e;
        }
    }

    private void onCredentialsObtained(Bundle extras, final MssoRequest request) {
        //Make credentials available to this request's MssoContext
        MASAuthCredentials creds = extras.getParcelable(MssoIntents.EXTRA_CREDENTIALS);
        request.getMssoContext().setCredentials(creds);
        //For AuthenticateRequest, we don't want to run it with new thread
        onProcessRequest(request);

    }

    private void onProcessAllPendingRequests() {
        final Collection<MssoRequest> requests = new ArrayList<>(MssoActiveQueue.getInstance().getAllRequest());
        for (MssoRequest mssoRequest : requests) {
            if (!mssoRequest.isRunning()) {
                startThreadedRequest(null, mssoRequest);
            }
        }
    }

    private void onProcessRequest(final MssoRequest request) {
        //The request is in running state
        request.setRunning(true);
        ResultReceiver receiver = request.getResultReceiver();

        MssoContext mssoContext = request.getMssoContext();
        try {
            MASResponse magResponse = mssoContext.executeRequest(request.getExtra(), request.getRequest());
            if (handleInterceptors(request.getId(), request.getRequest(), request.getExtra(), magResponse)) {
                //The request is intercepted and keep in the pending queue.
                return;
            }
            //Success: move to response queue and send a success notification.
            if (requestFinished(request)) {
                MssoResponse response = createMssoResponse(request, magResponse);
                MssoResponseQueue.getInstance().addResponse(response);
                respondSuccess(receiver, response.getId(), "OK");
            }
            //Otherwise, the request was cancelled, so don't bother enqueuing a response.

        } catch (CredentialRequiredException e) {
            if (DEBUG) Log.d(TAG, "Request for user credentials");
            //Notify listener
            MobileSsoListener mobileSsoListener = ConfigurationManager.getInstance().getMobileSsoListener();
            AuthenticationProvider authProvider = null;
            if (!MAS.isBrowserBasedAuthenticationEnabled()) {
                try {
                    authProvider = new OAuthClient(request.getMssoContext()).getSocialPlatformProvider(getApplicationContext());
                } catch (OAuthException | OAuthServerException e1) {
                    if (DEBUG) Log.e(TAG, e1.getMessage(), e1);
                    authProvider = null;
                }
            }
            if (mobileSsoListener != null) {
                mobileSsoListener.onAuthenticateRequest(request.getId(), authProvider);
            } else {
                if (DEBUG) Log.w(TAG, "No Authentication listener is registered");
            }
        } catch (TokenStoreUnavailableException e) {
            try {
                mssoContext.getTokenManager().getTokenStore().unlock();
            } catch (Exception e1) {
                handleErrorResponse(request, e1);
            }
        } catch (MAGServerException e) {
            if (handleInterceptors(request.getId(), request.getRequest(), request.getExtra(), e.getResponse())) {
                //The request is intercepted and keep in the pending queue.
                return;
            }
            handleErrorResponse(request, e);
        } catch (Exception e) {
            handleErrorResponse(request, e);
        } finally {
            //The request is not running, may or may not stay in the active queue
            request.setRunning(false);
        }
    }

    private void handleErrorResponse(MssoRequest request, Exception e) {
        if (DEBUG) Log.e(TAG, e.getMessage(), e);
        if (requestFinished(request)) {
            respondError(request.getResultReceiver(), new MAGError(e));
        }
    }

    /**
     * @return True to keep the request message to the queue
     */
    private boolean handleInterceptors(long requestId, MASRequest request, Bundle requestExtra, MASResponse response) {
        for (ResponseInterceptor ri : ConfigurationManager.getInstance().getResponseInterceptors()) {
            if (ri.intercept(requestId, request, requestExtra, response)) {
                return true;
            }
        }
        return false;
    }

    private MssoResponse createMssoResponse(MssoRequest request, MASResponse response) {
        return new MssoResponse(request, response);
    }

    /*
     * Find a request in either the inbound queue or the active queue.
     * If the request is in the inbound queue, move it to the active queue.
     * If this method returns a request, it is guaranteed to be present in the active queue.
     * @return located request or null.
     */
    private MssoRequest takeActiveRequest(long requestId) {
        MssoRequest request = MssoRequestQueue.getInstance().takeRequest(requestId);
        if (request != null) {
            MssoActiveQueue.getInstance().addRequest(request);
            return request;
        }
        request = MssoActiveQueue.getInstance().getRequest(requestId);
        return request;
    }

    private boolean requestFinished(MssoRequest request) {
        return null != MssoActiveQueue.getInstance().takeRequest(request.getId());
    }

    private void respondError(ResultReceiver receiver, MAGError error) {
        if (receiver != null) {
            Bundle resultData = new Bundle();
            resultData.putSerializable(MssoIntents.RESULT_ERROR, error);
            resultData.putString(MssoIntents.RESULT_ERROR_MESSAGE, error.getMessage());
            receiver.send(MssoIntents.RESULT_CODE_ERR, resultData);
        }
    }

    private void respondSuccess(ResultReceiver receiver, long requestId, String errorMessage) {
        if (receiver != null) {
            Bundle resultData = new Bundle();
            resultData.putString(MssoIntents.RESULT_ERROR_MESSAGE, errorMessage);
            resultData.putLong(MssoIntents.RESULT_REQUEST_ID, requestId);
            receiver.send(MssoIntents.RESULT_CODE_SUCCESS, resultData);
        }
    }
}
