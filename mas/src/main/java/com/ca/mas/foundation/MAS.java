/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.app.Activity;
import android.app.Application;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.core.MAG;
import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * The top level MAS object represents the Mobile App Services SDK in its entirety.
 * It is where the framework lifecycle begins, and ends if necessary.
 * It is the front facing class where many of the configuration settings for the SDK as a whole
 * can be found and utilized.
 */
public class MAS {

    public static Context ctx;
    private static Activity currentActivity;
    private static boolean hasRegisteredActivityCallback;
    private static MASAuthenticationListener masAuthenticationListener;
    private static int state;

    private static synchronized void init(@NonNull final Context context) {
        stop();
        // Initialize the MASConfiguration
        ctx = context.getApplicationContext();
        if (context instanceof Activity) {
            currentActivity = (Activity) context;
        }

        registerActivityLifecycleCallbacks((Application) ctx);

        // This is important, don't remove this
        new MASConfiguration(ctx);
        ConfigurationManager.getInstance().setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, final AuthenticationProvider provider) {
                if (masAuthenticationListener == null) {
                    Class<Activity> loginActivity = getLoginActivity();
                    if (loginActivity != null) {
                        Intent intent = new Intent(context, loginActivity);
                        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                        intent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, new MASAuthenticationProviders(provider));
                        context.startActivity(intent);
                    } else {
                        if (DEBUG)
                            Log.w(TAG, MASAuthenticationListener.class.getSimpleName() + " is required for user authentication.");
                    }
                } else {
                    masAuthenticationListener.onAuthenticateRequest(currentActivity, requestId, new MASAuthenticationProviders(provider));
                }
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

                if (masAuthenticationListener == null) {
                    Class<Activity> otpActivity = getOtpActivity();
                    if (otpActivity != null) {
                        Intent intent = new Intent(context, otpActivity);
                        intent.putExtra(MssoIntents.EXTRA_OTP_HANDLER, new MASOtpAuthenticationHandler(otpAuthenticationHandler));
                        context.startActivity(intent);
                    } else {
                        if (DEBUG)
                            Log.w(TAG, MASAuthenticationListener.class.getSimpleName() + " is required for otp authentication.");
                    }
                } else {
                    masAuthenticationListener.onOtpAuthenticateRequest(currentActivity, new MASOtpAuthenticationHandler(otpAuthenticationHandler));
                }


            }
        });
        MASConnectaManager.getInstance().start(ctx);
    }

    private static void registerActivityLifecycleCallbacks(Application application) {
        if (hasRegisteredActivityCallback) {
            return;
        } else {
            hasRegisteredActivityCallback = true;
        }
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });
    }

    /**
     * Return the MASLoginActivity from MASUI components if MASUI library is included in the classpath.
     *
     * @return A LoginActivity to capture the user credentials or null if error.
     */
    private static Class<Activity> getLoginActivity() {

        try {
            return (Class<Activity>) Class.forName("com.ca.mas.ui.MASLoginActivity");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return the MASOtpActivity from MASUI components if MASUI library is included in the classpath.
     *
     * @return A OtpActivity to capture the otp or null if error.
     */
    private static Class<Activity> getOtpActivity() {

        try {
            return (Class<Activity>) Class.forName("com.ca.mas.ui.otp.MASOtpActivity");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Turn on debug mode
     */
    public static void debug() {
        MAG.DEBUG = true;
    }

    /**
     * Starts the lifecycle of the MAS processes.
     * This will load the last used JSON configuration from storage. If there was none,
     * it will load from the default JSON configuration file (msso_config.json).
     */
    public static void start(@NonNull Context context) {
        init(context);
        MobileSsoFactory.getInstance(context);
        state = MASConstants.MAS_STATE_STARTED;
    }

    /**
     * Starts the lifecycle of the MAS processes.
     * This will load the default JSON configuration rather than from storage;
     * if the SDK was already initialized, this method will fully stop and restart the SDK.
     * The default JSON configuration file should be msso_config.json.
     * This will ignore the JSON configuration in the keychain storage and replace it with the default configuration.
     *
     * @param shouldUseDefault Boolean: using default configuration rather than the one in storage.
     */
    public static void start(@NonNull Context context, boolean shouldUseDefault) {
        init(context);
        MobileSsoFactory.getInstance(context, shouldUseDefault);
        state = MASConstants.MAS_STATE_STARTED;
    }

    /**
     * Starts the lifecycle of the MAS processes with the given JSON configuration data.
     * This method will (if it is different) overwrite the JSON configuration that was stored.
     *
     * @param jsonConfiguration JSON Configuration object.
     */
    public static void start(@NonNull Context context, JSONObject jsonConfiguration) {
        init(context);
        MobileSsoFactory.getInstance(context, jsonConfiguration);
        state = MASConstants.MAS_STATE_STARTED;
    }

    /**
     * Starts the lifecycle of the MAS processes with given JSON configuration file path.
     * This method will (if it is different) overwrite the JSON configuration that was stored.
     *
     * @param url URL of the JSON configuration file path.
     */
    public static void start(@NonNull Context context, URL url) {
        init(context);
        MobileSsoFactory.getInstance(context, url);
        state = MASConstants.MAS_STATE_STARTED;
    }

    /**
     * Starts the lifecycle of the MAS processes with given JSON configuration enrolment URL or null.
     * This method will overwrite JSON configuration (if they are different) that was stored in keychain when configuration file path or enrolment URL is provided.
     * When URL is recognized as null, this method will initialize SDK by using last used JSON configuration that is stored,
     * or load JSON configuration from defined default configuration file name.
     * <p>
     * Enrolment URL is an URL from gateway containing some of credentials required to establish secure connection.
     * The gateway must be configured to generate and handle enrolment process with client side SDK.
     * The enrolment URL can be retrieved in many ways which has to be configured properly along with the gateway in regards of the enrolment process.
     * MASFoundation SDK does not request, or retrieve the enrolment URL by itself.
     *
     * @param url      The enrollment URL
     *                 If the enrollment url is null, {@link MAS#start(Context)} will be used to start the
     *                 lifecycle of the MAS processes..
     * @param callback The callback to notify when a response is available, or if there is an error.
     */

    public static void start(@NonNull final Context context, final URL url, final MASCallback<Void> callback) {
        if (url == null) {
            try {
                MAS.start(context);
                Callback.onSuccess(callback, null);
            } catch (Exception e) {
                Callback.onError(callback, e);
            }
            return;
        }

        Uri uri = Uri.parse(url.toString());
        final String publicKeyHash = uri.getQueryParameter("subjectKeyHash");
        if (publicKeyHash == null || publicKeyHash.trim().isEmpty()) {
            Callback.onError(callback, new IllegalArgumentException("subjectKeyHash is not provided."));
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MAGHttpClient client = new MAGHttpClient(publicKeyHash);
                MAGRequest request = new MAGRequest.MAGRequestBuilder(url).
                        responseBody(MAGResponseBody.jsonBody()).build();
                try {
                    MAGResponse<JSONObject> response = client.execute(request);
                    if (response.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw ServerClient.createServerException(response, MASServerException.class);
                    }
                    MAS.start(context, response.getBody().getContent());
                    Callback.onSuccess(callback, null);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
                return null;
            }
        }.execute((Void) null);
    }

    /**
     * Request method for an HTTP POST, PUT, DELETE, GET call to the Gateway.
     *
     * @param request  The request to send.
     * @param callback The callback to notify when a response is available, or if there is an error.
     * @param <T>      To provide the data type of the expected response object. The SDK converts the
     *                 response stream data to the provided data type. By defining this generic type, it provides
     *                 tighter type checks at compile time. Currently the SDK support 2 types of response objects.
     *                 <ul>
     *                 <li>  application/json: {@link JSONObject}</li>
     *                 <li>  text/plain: {@link String}</li>
     *                 </ul>
     *                 Developers can define a response object type with {@link MASRequest.MASRequestBuilder#responseBody(MAGResponseBody)}.
     * @return The request ID.
     */
    public static <T> long invoke(final MASRequest request, final MASCallback<MASResponse<T>> callback) {

        return MobileSsoFactory.getInstance().processRequest(request, new MAGResultReceiver<T>(Callback.getHandler(callback)) {
            @Override
            public void onSuccess(final MAGResponse<T> response) {
                Callback.onSuccess(callback, new MASResponse<T>() {
                    public MASResponseBody<T> getBody() {
                        return new MASResponseBody<T>() {
                            @Override
                            public T getContent() {
                                if (response.getBody() == null) {
                                    return null;
                                }
                                return response.getBody().getContent();
                            }
                        };
                    }

                    @Override
                    public Map<String, List<String>> getHeaders() {
                        return response.getHeaders();
                    }

                    @Override
                    public int getResponseCode() {
                        return response.getResponseCode();
                    }

                    @Override
                    public String getResponseMessage() {
                        return response.getResponseMessage();
                    }
                });
            }

            @Override
            public void onError(MAGError error) {
                Callback.onError(callback, error);
            }

            @Override
            public void onRequestCancelled(Bundle data) {
                if (request.notifyOnCancel()) {
                    Callback.onError(callback, new RequestCancelledException(data));
                }
            }
        });
    }

    public static class RequestCancelledException extends Exception {

        private Bundle data;

        public RequestCancelledException(Bundle data) {
            this.data = data;
        }

        public Bundle getData() {
            return data;
        }

    }

    /**
     * Sets the name of the configuration file. This provides the ability to set the file name to a custom value.
     * To use a custom configuration name, you must call this method before {@link MAS#start(Context)}, {@link MAS#start(Context, boolean)}.
     *
     * @param filename The name of the configuration file.
     */
    public static void setConfigurationFileName(String filename) {
        ConfigurationManager.getInstance().setConfigurationFileName(filename);
    }

    /**
     * Retrieves a boolean indicating if the gateway is currently reachable or not.
     */
    public static void gatewayIsReachable(final MASCallback<Boolean> callback) {
        new AsyncTaskLoader<Void>(getContext()) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }

            @Override
            public Void loadInBackground() {
                try {
                    Callback.onSuccess(callback, InetAddress.getByName(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenHost()).isReachable(1000));
                } catch (IOException e) {
                    Callback.onSuccess(callback, false);
                }
                return null;
            }
        }.startLoading();
    }

    /**
     * Sets a listener to listen for connection events.
     *
     * @param listener The listener to listen for connection events.
     */
    public static void setConnectionListener(MASConnectionListener listener) {
        ConfigurationManager.getInstance().setConnectionListener(listener);
    }

    /**
     * Set a user login listener to handle user authentication.
     *
     * @param listener The user login listener to handle user authentication.
     */
    public static void setAuthenticationListener(MASAuthenticationListener listener) {
        masAuthenticationListener = listener;
    }

    /**
     * Sets the grant type property. The default is {@link MASConstants#MAS_GRANT_FLOW_CLIENT_CREDENTIALS}.
     *
     * @param type Either {@link MASConstants#MAS_GRANT_FLOW_CLIENT_CREDENTIALS} or {@link MASConstants#MAS_GRANT_FLOW_PASSWORD}.
     */
    public static void setGrantFlow(@MASGrantFlow int type) {
        switch (type) {
            case MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS:
                ConfigurationManager.getInstance().setDefaultGrantProvider(GrantProvider.CLIENT_CREDENTIALS);
                break;
            case MASConstants.MAS_GRANT_FLOW_PASSWORD:
                ConfigurationManager.getInstance().setDefaultGrantProvider(GrantProvider.PASSWORD);
                break;
            default:
                throw new MASRuntimeException(MAGErrorCode.TYPE_UNSUPPORTED, "Flow Type Unsupported");
        }
    }

    @Internal
    public static Context getContext() {
        return ctx;
    }

    @Internal
    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Cancels the specified request ID. If the response notification has not already been delivered
     * by the time this method executes, a response notification will never occur for the specified request ID
     * except {@link MASRequest.MASRequestBuilder#notifyOnCancel()} is set.
     *
     * @param requestId the request ID to cancel.
     */
    public static void cancelRequest(long requestId) {
        MobileSsoFactory.getInstance().cancelRequest(requestId, null);
    }

    /**
     * Cancels the specified request ID with additional information. If the response notification has not already been delivered
     * by the time this method executes, a response notification will never occur for the specified request ID
     * except {@link MASRequest.MASRequestBuilder#notifyOnCancel()} is set.
     * <p>
     * When {@link MASRequest.MASRequestBuilder#notifyOnCancel} is set, {@link MASCallback#onError(Throwable)}
     * will be triggered with {@link RequestCancelledException}.
     * The additional information can be retrieved with {@link RequestCancelledException#getData()}
     *
     * @param requestId the request ID to cancel.
     * @param data      the additional information to the request.
     */
    public static void cancelRequest(long requestId, Bundle data) {
        MobileSsoFactory.getInstance().cancelRequest(requestId, data);
    }

    /**
     * Cancels all requests. If the response notification has not already been delivered
     * by the time this method executes, a response notification will never occur,
     * except {@link MASRequest.MASRequestBuilder#notifyOnCancel()} is set.
     */
    public static void cancelAllRequests() {
        MobileSsoFactory.getInstance().cancelAllRequests(null);
    }

    /**
     * Cancels all requests with additional information. If the response notification has not already been delivered
     * by the time this method executes, a response notification will never occur,
     * except {@link MASRequest.MASRequestBuilder#notifyOnCancel()} is set.
     * <p>
     * When {@link MASRequest.MASRequestBuilder#notifyOnCancel} is set, {@link MASCallback#onError(Throwable)}
     * will be triggered with {@link RequestCancelledException}.
     * The additional information can be retrieved with {@link RequestCancelledException#getData()}
     *
     * @param data the additional information to the request.
     */
    public static void cancelAllRequest(Bundle data) {
        MobileSsoFactory.getInstance().cancelAllRequests(data);
    }

    /**
     * <p>Requests that any pending queued requests be processed.</p>
     * <p>This can be called from an activity's onResume() method to ensure that
     * any pending requests waiting for an initial unlock code to be set
     * on the device get a chance to continue.</p>
     * <p>This method immediately returns to the calling thread.</p>
     * <p>An activity may be started if a device lock code (still) needs to be configured
     * or if the user must be prompted for a username and password.</p>
     */
    public static void processPendingRequests() {
        MobileSsoFactory.getInstance().processPendingRequests();
    }

    /**
     * Returns current {@link MASState} value.  The value can be used to determine which state SDK is currently at.
     *
     * @return return {@link MASState} of current state.
     */
    public static @MASState int getState(Context context) {
        if (state != 0) {
            return state;
        }
        // Determine the state
        ConfigurationManager.getInstance().init(context);
        try {
            ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
            ConfigurationManager.getInstance().reset();
            state = MASConstants.MAS_STATE_NOT_INITIALIZED;
        } catch (Exception e) {
            state = MASConstants.MAS_STATE_NOT_CONFIGURED;
        }
        return state;
    }

    /**
     * Enable PKCE extension to OAuth.
     *
     * @param enablePKCE True to enable PKCE extension, False to disable PKCE Extension. Default to true.
     */
    public static void enablePKCE(boolean enablePKCE) {
        ConfigurationManager.getInstance().enablePKCE(enablePKCE);
    }

    /**
     * Determines whether PKCE extension is enabled.
     * @return true if PKCE extension is enabled, false otherwise
     */
    public static boolean isPKCEEnabled() {
        return ConfigurationManager.getInstance().isPKCEEnabled();
    }

    /**
     * Stops the lifecycle of all MAS processes.
     */
    public static void stop() {
        state = MASConstants.MAS_STATE_STOPPED;
    }
}
