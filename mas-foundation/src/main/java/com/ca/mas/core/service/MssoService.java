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
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.clientcredentials.ClientCredentialsException;
import com.ca.mas.core.clientcredentials.ClientCredentialsServerException;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.oauth.OAuthClient;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.policy.exceptions.CredentialRequiredException;
import com.ca.mas.core.policy.exceptions.LocationInvalidException;
import com.ca.mas.core.policy.exceptions.LocationRequiredException;
import com.ca.mas.core.policy.exceptions.MobileNumberInvalidException;
import com.ca.mas.core.policy.exceptions.MobileNumberRequiredException;
import com.ca.mas.core.policy.exceptions.OtpException;
import com.ca.mas.core.policy.exceptions.TokenStoreUnavailableException;
import com.ca.mas.core.registration.DeviceRegistrationAwaitingActivationException;
import com.ca.mas.core.registration.RegistrationException;
import com.ca.mas.core.registration.RegistrationServerException;
import com.ca.mas.core.request.internal.AuthenticateRequest;
import com.ca.mas.core.token.JWTExpiredException;
import com.ca.mas.core.token.JWTInvalidAUDException;
import com.ca.mas.core.token.JWTInvalidAZPException;
import com.ca.mas.core.token.JWTInvalidSignatureException;
import com.ca.mas.core.token.JWTValidationException;
import com.ca.mas.foundation.MASAuthCredentials;

import java.io.IOException;
import java.net.HttpURLConnection;
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
            if (DEBUG) Log.d(TAG, "Request ID not found, assuming request is canceled or already processed");
            return;
        }

        if (MssoIntents.ACTION_PROCESS_REQUEST.equals(action)) {
            startThreadedRequest(request);
            return;
        } else if (MssoIntents.ACTION_CREDENTIALS_OBTAINED.equals(action)) {
            onCredentialsObtained(extras, request);
            return;
        } else if (MssoIntents.ACTION_VALIDATE_OTP.equals(action)) {
            //Once we retrieve the OTP from the user
            onOtpObtained(extras, request);
            return;
        }

        if (DEBUG) Log.w(TAG, "Ignoring intent with unrecognized action " + action);
    }

    private void startThreadedRequest(final MssoRequest request) {
        MssoExecutorService.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                onProcessRequest(request);
            }
        });
    }

    private void onOtpObtained(Bundle extras, MssoRequest request) {
        Bundle bundle = new Bundle();
        bundle.putString(OtpConstants.X_OTP, extras.getString(MssoIntents.EXTRA_OTP_VALUE));
        bundle.putString(OtpConstants.X_OTP_CHANNEL, extras.getString(MssoIntents.EXTRA_OTP_SELECTED_CHANNELS));
        //Associate the OTP and selected channels to the request
        request.setExtra(bundle);
        startThreadedRequest(request);
    }

    private void onCredentialsObtained(Bundle extras, final MssoRequest request) {
        //Make credentials available to this request's MssoContext
        MASAuthCredentials creds = extras.getParcelable(MssoIntents.EXTRA_CREDENTIALS);
        request.getMssoContext().setCredentials(creds);

        boolean originalRequestProcessed = false;

        //Give highest priority to any authenticate requests
        if (request.getRequest() instanceof AuthenticateRequest) {
            originalRequestProcessed = true;
            onProcessRequest(request);
        }

        final ArrayList<MssoRequest> requests = new ArrayList<>(MssoActiveQueue.getInstance().getAllRequest());
        for (final MssoRequest mssoRequest : requests) {
            if (request == mssoRequest) {
                originalRequestProcessed = true;
            }
            startThreadedRequest(mssoRequest);
        }

        //Ensure we make at least one request to process the original request
        if (!originalRequestProcessed) {
            startThreadedRequest(request);
        }
    }

    private void onProcessAllPendingRequests() {
        final Collection<MssoRequest> requests = new ArrayList<>(MssoActiveQueue.getInstance().getAllRequest());
        for (MssoRequest mssoRequest : requests) {
            startThreadedRequest(mssoRequest);
        }
    }

    private void onProcessRequest(final MssoRequest request) {
        ResultReceiver receiver = request.getResultReceiver();
        boolean expectingUnlock = false;

        MssoContext mssoContext = request.getMssoContext();
        try {
            MAGResponse magResponse = mssoContext.executeRequest(request.getExtra(), request.getRequest());

            //Success: move to response queue and send a success notification.
            if (requestFinished(request)) {
                MssoResponse response = createMssoResponse(request, magResponse);
                MssoResponseQueue.getInstance().addResponse(response);
                respondSuccess(receiver, response.getId(), "OK");
            }
            //Otherwise, the request was cancelled, so don't bother enqueuing a response.

            MssoState.setExpectingUnlock(false);
        } catch (CredentialRequiredException e) {
            if (DEBUG) Log.d(TAG, "Request for user credentials");
            //Notify listener
            MobileSsoListener mobileSsoListener = ConfigurationManager.getInstance().getMobileSsoListener();
            try {
                AuthenticationProvider authProvider = new OAuthClient(request.getMssoContext()).getSocialPlatformProvider(getApplicationContext());
                if (mobileSsoListener != null) {
                    mobileSsoListener.onAuthenticateRequest(request.getId(), authProvider);
                } else {
                    if (DEBUG) Log.w(TAG, "No Authentication listener is registered");
                }
            } catch (OAuthException | OAuthServerException e1) {
                if (DEBUG) Log.e(TAG, e1.getMessage(), e1);
                requestFinished(request);
                respondError(request.getResultReceiver(), MssoIntents.RESULT_CODE_ERR_AUTHORIZE, new MAGError(e1));
            }
        } catch (TokenStoreUnavailableException e) {
            try {
                expectingUnlock = true;
                mssoContext.getTokenManager().getTokenStore().unlock();
            } catch (Exception e1) {
                requestFinished(request);
                respondError(receiver, MssoIntents.RESULT_CODE_ERR_UNKNOWN, new MAGError(e));
            }
        } catch (OtpException e) {
            OtpResponseHeaders otpResponseHeaders = e.getOtpResponseHeaders();
            MobileSsoListener mobileSsoListener = ConfigurationManager.getInstance().getMobileSsoListener();

            if (mobileSsoListener != null) {
                if (OtpResponseHeaders.X_OTP_VALUE.REQUIRED.equals(otpResponseHeaders.getxOtpValue())) {
                    mobileSsoListener.onOtpAuthenticationRequest(new OtpAuthenticationHandler(request.getId(), otpResponseHeaders.getChannels(), false, null));
                } else if (OtpResponseHeaders.X_CA_ERROR.OTP_INVALID == otpResponseHeaders.getErrorCode()) {
                    Bundle extra = request.getExtra();
                    String selectedChannels = null;
                    if (extra != null) {
                        selectedChannels = extra.getString(OtpConstants.X_OTP_CHANNEL);
                    }

                    OtpAuthenticationHandler otpHandler = new OtpAuthenticationHandler(request.getId(), otpResponseHeaders.getChannels(), true, selectedChannels);
                    mobileSsoListener.onOtpAuthenticationRequest(otpHandler);
                }
                return;
            }
            if (DEBUG) Log.e(TAG, e.getMessage(), e);
            requestFinished(request);
            respondError(receiver, getErrorCode(e), new MAGError(e));
        } catch (Exception e2) {
            if (DEBUG) Log.e(TAG, e2.getMessage(), e2);
            requestFinished(request);
            respondError(receiver, getErrorCode(e2), new MAGError(e2));
        } finally {
            MssoState.setExpectingUnlock(expectingUnlock);
        }
    }

    private int getErrorCode(Throwable exception) {
        try {
            throw exception;
        } catch (DeviceRegistrationAwaitingActivationException e) {
            return MssoIntents.RESULT_CODE_ERR_AWAITING_REGISTRATION;
        } catch (ClientCredentialsException | ClientCredentialsServerException e) {
            return MssoIntents.RESULT_CODE_ERR_CLIENT_CREDENTIALS;
        } catch (RegistrationException | RegistrationServerException e) {
            return MssoIntents.RESULT_CODE_ERR_REGISTRATION;
        } catch (OAuthException | OAuthServerException e) {
            return MssoIntents.RESULT_CODE_ERR_OAUTH;
        } catch (LocationRequiredException e) {
            return MssoIntents.RESULT_CODE_ERR_LOCATION_REQUIRED;
        } catch (LocationInvalidException e) {
            return MssoIntents.RESULT_CODE_ERR_LOCATION_UNAUTHORIZED;
        } catch (MobileNumberRequiredException e) {
            return MssoIntents.RESULT_CODE_ERR_MSISDN_REQUIRED;
        } catch (MobileNumberInvalidException e) {
            return MssoIntents.RESULT_CODE_ERR_MSISDN_UNAUTHORIZED;
        } catch (JWTInvalidAUDException e) {
            return MssoIntents.RESULT_CODE_ERR_JWT_AUD_INVALID;
        } catch (JWTInvalidAZPException e) {
            return MssoIntents.RESULT_CODE_ERR_JWT_AZP_INVALID;
        } catch (JWTExpiredException e) {
            return MssoIntents.RESULT_CODE_ERR_JWT_EXPIRED;
        } catch (JWTInvalidSignatureException e) {
            return MssoIntents.RESULT_CODE_ERR_JWT_SIGNATURE_INVALID;
        } catch (JWTValidationException e) {
            return MssoIntents.RESULT_CODE_ERR_JWT_INVALID;
        } catch (AuthenticationException e) {
            return HttpURLConnection.HTTP_UNAUTHORIZED;
        } catch (IOException e) {
            return MssoIntents.RESULT_CODE_ERR_IO;
        } catch (Throwable e) {
            if (e.getCause() == null) {
                return MssoIntents.RESULT_CODE_ERR_UNKNOWN;
            } else {
                return getErrorCode(e.getCause());
            }
        }
    }

    private MssoResponse createMssoResponse(MssoRequest request, MAGResponse response) {
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
        // TODO check if another service thread is already processing this request, if we later give the service more than one thread
        return request;
    }

    private boolean requestFinished(MssoRequest request) {
        return null != MssoActiveQueue.getInstance().takeRequest(request.getId());
    }

    private void respondError(ResultReceiver receiver, int resultCode, MAGError error) {
        if (receiver != null) {
            Bundle resultData = new Bundle();
            resultData.putSerializable(MssoIntents.RESULT_ERROR, error);
            resultData.putString(MssoIntents.RESULT_ERROR_MESSAGE, error.getMessage());
            receiver.send(resultCode, resultData);
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
