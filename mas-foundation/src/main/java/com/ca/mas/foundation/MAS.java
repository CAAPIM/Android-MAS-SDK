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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.service.MssoServiceState;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.token.JWTValidatorFactory;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * The top level MAS object represents the Mobile App Services SDK in its entirety.
 * It is where the framework lifecycle begins, and ends if necessary.
 * It is the front facing class where many of the configuration settings for the SDK as a whole
 * can be found and utilized.
 */
public class MAS {

    public static final String TAG = "MAS";
    public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);

    private static Context appContext;
    private static Activity currentActivity;
    private static boolean hasRegisteredActivityCallback;
    private static MASAuthenticationListener masAuthenticationListener;
    private static MASOtpMultiFactorAuthenticator otpMultiFactorAuthenticator = new MASOtpMultiFactorAuthenticator();
    private static int state;

    private static LinkedHashMap<Class, MASLifecycleListener> masLifecycleListener = new LinkedHashMap<>();


    private static boolean browserBasedAuthenticationEnabled = false;

    private MAS() {
    }

    static {
        EventDispatcher.STARTED.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

                if (!masLifecycleListener.isEmpty())
                    for(MASLifecycleListener listner: masLifecycleListener.values()){
                        listner.onStarted();
                    }
            }
        });
    }

    private static synchronized void init(@NonNull final Context context) {
        stop();
        // Initialize the MASConfiguration
        appContext = context.getApplicationContext();
        if (context instanceof Activity) {
            currentActivity = (Activity) context;
        }

        registerActivityLifecycleCallbacks((Application) appContext);

        // This is important, don't remove this
        new MASConfiguration(appContext);
        ConfigurationManager.getInstance().setMobileSsoListener(new AuthenticationListener(appContext));
        registerMultiFactorAuthenticator(otpMultiFactorAuthenticator);
        if (isAlgoRS256() || isPreloadJWKSEnabled())
            addLifeCycleListener(new JWKPreLoadListener());
    }

    private static boolean isAlgoRS256() {
        return  JWTValidatorFactory.Algorithm.RS256.toString().equals(MASConfiguration.getCurrentConfiguration().getIdTokenSignAlg());
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
                if (currentActivity != null) {
                    Activity currentActivity = MAS.currentActivity;
                    if (currentActivity == activity) {
                        MAS.currentActivity = null;
                    }
                }
            }
        });
    }


    /**
     * Turn on debug mode
     */
    public static void debug() {
        MAS.DEBUG = true;
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
        EventDispatcher.STARTED.notifyObservers();
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
        EventDispatcher.STARTED.notifyObservers();
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
        EventDispatcher.STARTED.notifyObservers();
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
        EventDispatcher.STARTED.notifyObservers();
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

        if (appContext == null) {
            appContext = context.getApplicationContext();
        }

        final Uri uri = Uri.parse(url.toString());
        final String publicKeyHash = uri.getQueryParameter("subjectKeyHash");
        if (publicKeyHash == null || publicKeyHash.trim().isEmpty()) {
            Callback.onError(callback, new IllegalArgumentException("subjectKeyHash is not provided."));
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //remove url safe
                    String pkh = Base64.encodeToString(Base64.decode(publicKeyHash, Base64.NO_WRAP | Base64.URL_SAFE), Base64.NO_WRAP);
                    MASSecurityConfiguration enrollmentConfig = new MASSecurityConfiguration.Builder()
                            .add(pkh)
                            .host(uri)
                            .build();
                    MAGHttpClient client = new MAGHttpClient();
                    MASRequest request = new MASRequest.MASRequestBuilder(url).
                            responseBody(MASResponseBody.jsonBody()).setPublic().build();
                    MASResponse<JSONObject> response = client.execute(request, enrollmentConfig);
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
     *                 Developers can define a response object type with {@link MASRequest.MASRequestBuilder#responseBody(MASResponseBody)} )}.
     * @return The request ID.
     */
    public static <T> long invoke(final MASRequest request, final MASCallback<MASResponse<T>> callback) {

        return MobileSsoFactory.getInstance().processRequest(request, new MAGResultReceiver<T>(Callback.getHandler(callback)) {
            @Override
            public void onSuccess(final MASResponse<T> response) {
                Callback.onSuccess(callback, new MASResponseProxy<T>(response));
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
        private final Bundle data;

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
     * Get a user login listener to handle user authentication.
     */
    static MASAuthenticationListener getAuthenticationListener() {
        return masAuthenticationListener;
    }

    /**
     * Sets a listener to listen for MAS lifecycle events.
     *
     * @param listner Listener that listens for MAS lifecycle events.
     */
    private static void addLifeCycleListener(MASLifecycleListener listner) {
        masLifecycleListener.put(listner.getClass(), listner);
    }

    /**
     * Checks whether the consumer of MAS has set any authentication listener or not.
     * This would help other frameworks to override the listener (and the login UI) as a fallback instead of default
     * implementation of MASUI.
     *
     * @return boolean True if user has set an authentication listener, else false.
     */
    public static boolean isAuthenticationListenerRegistered() {
        return masAuthenticationListener != null;
    }

    /**
     * Sets the grant type property. The default is {@link MASConstants#MAS_GRANT_FLOW_CLIENT_CREDENTIALS}.
     *
     * @param type Either {@link MASConstants#MAS_GRANT_FLOW_CLIENT_CREDENTIALS} or {@link MASConstants#MAS_GRANT_FLOW_PASSWORD}.
     */
    public static void setGrantFlow(@MASGrantFlow int type) {
        switch (type) {
            case MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS:
                ConfigurationManager.getInstance().setDefaultGrantProvider(MASGrantProvider.CLIENT_CREDENTIALS);
                break;
            case MASConstants.MAS_GRANT_FLOW_PASSWORD:
                ConfigurationManager.getInstance().setDefaultGrantProvider(MASGrantProvider.PASSWORD);
                break;
            default:
                throw new IllegalArgumentException("Invalid Flow Type");
        }
    }

    @Internal
    public static Context getContext() {
        return appContext;
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
    public static
    @MASState
    int getState(Context context) {
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
     * Enable JWKS preload. If enabled, the JWKS is preloaded when the SDK is started.
     *
     * @param enablePreloadJwks True to enable preloading of JWKS, False if preload is not needed.
     */
    public static void enableJwksPreload(boolean enablePreloadJwks) {
        ConfigurationManager.getInstance().enableJwksPreload(enablePreloadJwks);
    }
    /**
     *  Value of the boolean indicator which indicate if the JWKS should be preloaded.
     */
    public static boolean isPreloadJWKSEnabled() {
        return  ConfigurationManager.getInstance().isJwksPreloadEnabled();
    }

    /**
     *  Sets boolean indicator of enforcing id_token validation upon device registration/user authentication. id_token is being validated as part of authentication/registration process against known signing algorithm.
     *  Mobile SDK currently supports following algorithm(s):HS256
     *  Any other signing algorithm will cause authentication/registration failure due to unknown signing algorithm.
     *  If the server side is configured to return a different or custom algorithm, ensure to disable id_token validation to avoid any failure on Mobile SDK.
     *  By default, id_token validation is enabled and enforced in authentication and/or registration process; it can be opted-out.
     *  @param enableValidation BOOL value of indicating whether id_token validation is enabled or not.
     */
    public static void enableIdTokenValidation(boolean enableValidation) {
        ConfigurationManager.getInstance().enableIdTokenValidation(enableValidation);
    }

    /**
     *  Value of the boolean indicator which indicate if the id_token validation is active or not.
     */
    public static boolean isIdTokenValidationEnabled(){
        return ConfigurationManager.getInstance().isIdTokenValidationEnabled();
    }

    /**
     * Determines whether PKCE extension is enabled.
     *
     * @return true if PKCE extension is enabled, false otherwise
     */
    public static boolean isPKCEEnabled() {
        return ConfigurationManager.getInstance().isPKCEEnabled();
    }

    /**
     * Stops the lifecycle of all MAS processes.
     */
    public static void stop() {
        if (appContext != null && MssoServiceState.getInstance().getServiceConnection() != null) {
            appContext.unbindService(MssoServiceState.getInstance().getServiceConnection());
            MssoServiceState.getInstance().setBound(false);
            MssoServiceState.getInstance().setServiceConnection(null);
        }
        state = MASConstants.MAS_STATE_STOPPED;
        EventDispatcher.STOP.notifyObservers();
        MobileSsoFactory.reset();
        appContext = null;
    }


    /**
     * Signs the provided JWT {@link MASClaims} object with the device registered private key using SHA-256 hash algorithm
     * and injects JWT claims based on the user information.
     * This method will use a default value of 5 minutes for the JWS 'exp' claim if not provided.
     *
     * @param masClaims The JWT Claims
     * @return The JWT format consisting of Base64URL-encoded parts delimited by period ('.') characters.
     * @throws MASException Failed to sign
     */
    public static String sign(final MASClaims masClaims) throws MASException {
        return sign(masClaims, StorageProvider.getInstance().getTokenManager().getClientPrivateKey());
    }

    /**
     * Signs the provided JWT {@link MASClaims} object with the provided RSA private key using SHA-256 hash algorithm
     * and injects JWT claims based on the user information.
     * This method will use a default value of 5 minutes for the JWS 'exp' claim if not provided.
     *
     * @param masClaims  The JWT Claims
     * @param privateKey The private RSA key.
     * @return The JWT format consisting of Base64URL-encoded parts delimited by period ('.') characters.
     * @throws MASException Failed to sign
     */
    public static String sign(MASClaims masClaims, PrivateKey privateKey) throws MASException {
        if (!MASDevice.getCurrentDevice().isRegistered()) {
            throw new IllegalStateException("Device not registered.");
        }
        return JWTSign.sign(masClaims, privateKey);

    }

    /**
     * Enables Browser Based Authentication for authorization (set to false by default).
     * Browser Based Authentication allows administrators to configure the login screen
     * which will be rendered in the browser allowing users to log in.
     */
    public static void enableBrowserBasedAuthentication() {
        browserBasedAuthenticationEnabled = true;
    }

    /**
     * Checks if is enabled or not for authorization (returns false by default).
     *
     * @return true if Browser Based Login is enabled and false otherwise
     */
    public static boolean isBrowserBasedAuthenticationEnabled() {
        return browserBasedAuthenticationEnabled;
    }

    /**
     * Static method to register custom {@link MASMultiFactorAuthenticator} object to handle multi-factor authentication.
     * The registered {@link MASOtpMultiFactorAuthenticator} will be executed in registration order.
     *
     * @param authenticator The {@link MASOtpMultiFactorAuthenticator} that handle the Multi Factor Authenticator
     */
    public static void registerMultiFactorAuthenticator(MASMultiFactorAuthenticator authenticator) {
        ConfigurationManager.getInstance().registerResponseInterceptor(authenticator);
    }

    /**
     * Unregister a previously registered {@link MASOtpMultiFactorAuthenticator}.
     *
     * @param authenticator The {@link MASOtpMultiFactorAuthenticator} to unregister.
     */
    private static void unregisterMultiFactorAuthenticator(MASMultiFactorAuthenticator authenticator) {
        ConfigurationManager.getInstance().unregisterResponseInterceptor(authenticator);
    }

    /**
     * Uploads multipart form-data to server.
     *
     * @param  request            The {@link MASRequest} to upload multipart form-data, required.
     * @param  multipart          The multipart body  {@link MultiPart}, required.
     * @param  progressListener   The  {@link MASProgressListener} to receive progress, optional.
     * @param  callback           The {@link MASCallback}, required.
     * @throws MASException       If network call fails due to various reasons.
     * @throws MAGRuntimeException If multipart is null or file part and form fields, both are empty.
     * @return
     */
    public static long postMultiPartForm(MASRequest request, MultiPart multipart, MASProgressListener progressListener, MASCallback callback) throws MASException, MAGRuntimeException {
        if(multipart == null || (multipart.getFilePart().isEmpty() && multipart.getFormFields().isEmpty())){
            throw new MAGRuntimeException(MAGErrorCode.INVALID_REUEST, "Multipart body empty");
        }
        MASRequest masRequest = new MASRequest.MASRequestBuilder(request).post(MASRequestBody.multipartBody(multipart, progressListener)).
                build();
        return MAS.invoke(masRequest, callback);
    }

    /**
     * Downloads a file from server saves in the filePath.
     * @param request The {@link MASRequest} to upload multipart form-data.
     * @param callback The {@link MASCallback}.
     * @param filePath The {@link MASFileObject} contains the folder and name of file to save the download.
     * @param progressListener The  {@link MASProgressListener} to receive progress.
     */
    /*public static void downloadFile(MASRequest request, final MASCallback callback, MASFileObject filePath, MASProgressListener progressListener) throws MAGRuntimeException {
        if (filePath.getFilePath() == null  || filePath.getFileName() == null ){
            throw new MAGRuntimeException(MAGErrorCode.INVALID_INPUT,"Either file path or file name is missing");
        }
        if(request.getHeaders().get("request-type")== null){
            throw new MAGRuntimeException(MAGErrorCode.INVALID_INPUT,"'request-type' header missing", null);
        }
        MASRequest.MASRequestBuilder downloadRequestBuilder = new MASRequest.MASRequestBuilder(request);
        downloadRequestBuilder.setDownloadFile(filePath);
        downloadRequestBuilder.progressListener(progressListener);
        MASRequest downloadRequest = downloadRequestBuilder.build();
        MAS.invoke(downloadRequest, callback);
    }*/
}