/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import com.ca.mas.core.auth.AuthResultReceiver;
import com.ca.mas.core.auth.ble.BluetoothLePeripheral;
import com.ca.mas.core.auth.ble.BluetoothLePeripheralCallback;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.conf.Server;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.oauth.OAuthClient;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.MssoClient;
import com.ca.mas.core.service.MssoIntents;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Entry point for the Mobile SSO SDK.
 */
public final class MobileSsoFactory {
    private static final AtomicReference<MobileSso> mobileSso = new AtomicReference<MobileSso>();

    private MobileSsoFactory() {
    }

    /**
     * <p>Obtains the cached {@link MobileSso} instance.</p>
     * If a MobileSso instance has not been initialized with {@link #getInstance(Context, JSONObject)} or
     * {@link #getInstance(Context)}, throw {@link IllegalStateException}.
     *
     * @return A MobileSso implementation
     */
    public static MobileSso getInstance() {
        MobileSso ret = mobileSso.get();
        if (ret != null) {
            return ret;
        } else {
            throw new IllegalStateException("Mobile SSO has not been initialized.");
        }
    }

    /**
     * <p>Obtains and initializes the {@link MobileSso} instance using the specified Context.</p>
     * If a MobileSso instance has already been created for this process, it will be returned.
     * Otherwise, a new MobileSso instance will be created using the specified context and last
     * active JSON Configuration file.
     *
     * @param context      Context to provide access to services such as the device ID and current location.  Required.
     * @param forceDefault True - force to load the JSON Configuration under /assets/msso_config.json
     * @return a MobileSso implementation.  Never null.
     */
    public static MobileSso getInstance(@NonNull Context context, boolean forceDefault) {

        ConfigurationManager.getInstance().init(context);
        if (forceDefault) {
            ConfigurationManager.getInstance().activateDefault();
        } else {
            MobileSso ret = mobileSso.get();
            if (ret != null) {
                return ret;
            }
        }

        return getInstance(context, ConfigurationManager.getInstance().getConnectedGatewayConfig());
    }

    /**
     * <p>Obtains and initializes the {@link MobileSso} instance using the specified Context.</p>
     * If a MobileSso instance has already been created for this process, it will be returned.
     * Otherwise, a new MobileSso instance will be created using the specified context and last
     * active JSON Configuration file.
     *
     * @param context Context to provide access to services such as the device ID and current location.  Required.
     * @return a MobileSso implementation.  Never null.
     */
    public static MobileSso getInstance(@NonNull Context context) {
        return getInstance(context, false);
    }

    /**
     * <p>Obtains and initializes the {@link MobileSso} instance using the specified Context and file.</p>
     * <p>If a MobileSso instance has already been created with the same host, port and prefix for this process,
     * it will be returned, otherwise, a new MobileSso instance will be created using the specified
     * context and JSONObject. Multiple MobileSso with different configuration is not currently supported,
     * Application should not use more than one instance of MobileSso with different setting.
     * </p>
     * <p>You should use this method cautiously at runtime as an exception may be thrown due to an invalid
     * JSON Object, missing mandatory value, invalid types, etc. You may use a try and catch block
     * to avoid applications from being unexpectedly killed.
     * </p>
     *
     * @param context Context to provide access to services such as the device ID and current location.  Required.
     * @param url     URL of JSON configuration to use, if a MobileSso has not yet been created for this process.  Required.
     * @return a MobileSso implementation.  Never null.
     */
    public static MobileSso getInstance(@NonNull Context context, @NonNull URL url) {
        if (!"file".equalsIgnoreCase(url.getProtocol())) {
            throw new MAGRuntimeException(MAGErrorCode.INVALID_URL, "Invalid URL, only file URL is allowed");
        }

        ConfigurationManager.getInstance().init(context);
        InputStream is = null;
        try {
            is = url.openStream();
            JSONObject jsonObject = ConfigurationManager.getInstance().getConfig(is);
            return getInstance(context, jsonObject);
        } catch (IOException e) {
            throw new MAGRuntimeException(MAGErrorCode.INVALID_JSON, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * <p>Obtains and initializes the {@link MobileSso} instance using the specified Context and configuration JSONObject.</p>
     * <p>If a MobileSso instance has already been created with the same host, port and prefix for this process,
     * it will be returned, otherwise, a new MobileSso instance will be created using the specified
     * context and JSONObject. Multiple MobileSso with different configuration is not currently supported,
     * Application should not use more than one instance of MobileSso with different setting.
     * </p>
     * <p>You should use this method cautiously at runtime as an exception may be thrown due to an invalid
     * JSON Object, missing mandatory value, invalid types, etc. You may use a try and catch block
     * to avoid applications from being unexpectedly killed.
     * </p>
     *
     * @param context Context to provide access to services such as the device ID and current location.  Required.
     * @param config  JSON configuration to use, if a MobileSso has not yet been created for this process.  Required.
     * @return a MobileSso implementation.  Never null.
     */
    public static MobileSso getInstance(@NonNull Context context, @NonNull JSONObject config) {

        synchronized (mobileSso) {

            ConfigurationManager.getInstance().init(context);
            boolean isSwitching = false;

            if (isSwitchGateway(config)) {
                EventDispatcher.BEFORE_GATEWAY_SWITCH.notifyObservers();
                isSwitching = true;
            }

            ConfigurationManager.getInstance().activate(config);
            mobileSso.set(createMobileSso(context));

            if (isSwitching) {
                EventDispatcher.AFTER_GATEWAY_SWITCH.notifyObservers();
            }

            return mobileSso.get();
        }
    }

    private static boolean isSwitchGateway(JSONObject newConfig) {
        Server current = ConfigurationManager.getInstance().getConnectedGateway();
        return !current.equals(new Server(newConfig));
    }

    /**
     * Resets the initialized MobileSso. If a MobileSso instance has already been created for this process, it
     * will be reset, and a new MobileSso instance will be created during the next call of
     * {@link #getInstance()} or {@link #getInstance(Context, JSONObject)}
     */
    public static void reset() {
        synchronized (mobileSso) {
            mobileSso.set(null);
            ConfigurationManager.getInstance().reset();
        }
    }

    private static MobileSso createMobileSso(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final MssoContext mssoContext = MssoContext.newContext();
        mssoContext.init(applicationContext);
        mssoContext.initPolicyManager();

        final MssoClient mssoClient = new MssoClient(mssoContext, applicationContext);
        final BluetoothLePeripheral bleServer = BluetoothLePeripheral.getInstance();
        bleServer.init(mssoContext.getConfigurationProvider(), applicationContext);

        return new MobileSso() {

            @Override
            public long processRequest(MAGRequest request, ResultReceiver resultReceiver) {
                return mssoClient.processRequest(request, resultReceiver);
            }

            @Override
            public void logout(boolean contactServer) {
                mssoContext.logout(contactServer);
            }

            @Override
            public void destroyAllPersistentTokens() {
                EventDispatcher.RESET_LOCALLY.notifyObservers();
                mssoContext.destroyAllPersistentTokens();
            }

            @Override
            public void removeDeviceRegistration() {
                mssoContext.removeDeviceRegistration();
                mssoContext.destroyPersistentTokens();
            }

            @Override
            public boolean isAppLogon() {
                return mssoContext.isAppLogon();
            }

            @Override
            public boolean isLogin() {
                return mssoContext.isLogin();
            }

            @Override
            public String getUserProfile() {
                return mssoContext.getUserProfile();
            }

            @Override
            public void logoffApp() {
                mssoContext.logoffApp();
            }

            @Override
            public void logoutDevice() {
                removeDeviceRegistration();
            }

            @Override
            public boolean isDeviceRegistered() {
                return mssoContext.isDeviceRegistered();
            }

            @Override
            public ConfigurationProvider getConfigurationProvider() {
                return mssoContext.getConfigurationProvider();
            }

            @Override
            public void authorize(String url, ResultReceiver resultReceiver) {
                //TODO Handle for QRCode and NFC with the same url string
                MAGRequest.MAGRequestBuilder builder;
                try {
                    if (url == null || url.trim().length() == 0) {
                        throw new IllegalArgumentException("Authorization request cannot be empty.");
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(url);
                        String provider_url = jsonObject.getString("provider_url");
                        if (resultReceiver instanceof AuthResultReceiver) {
                            ((AuthResultReceiver) resultReceiver).setJson(jsonObject);
                        }
                        builder = new MAGRequest.MAGRequestBuilder(getURI(provider_url));
                    } catch (JSONException e) {
                        builder = new MAGRequest.MAGRequestBuilder(getURI(url));
                    }
                } catch (Exception e) {
                    if (resultReceiver != null) {
                        Bundle result = new Bundle();
                        result.putString(MssoIntents.RESULT_ERROR_MESSAGE, e.getMessage());
                        resultReceiver.send(MssoIntents.RESULT_CODE_ERR_UNKNOWN, result);
                    }
                    return;
                }
                processRequest(builder.build(), resultReceiver);
            }

            @Override
            public void startBleSessionSharing(BluetoothLePeripheralCallback client) {
                bleServer.start(client);
            }

            @Override
            public void stopBleSessionSharing() {
                bleServer.stop();
            }

            @Override
            public URI getURI(String relativePath) {
                return mssoContext.getConfigurationProvider().getUri(relativePath);
            }

            @Override
            public String getPrefix() {
                return mssoContext.getConfigurationProvider().getPrefix();
            }

            @Override
            public AuthenticationProvider getAuthenticationProvider() throws Exception {
                return new OAuthClient(mssoContext).getSocialPlatformProvider(applicationContext);
            }

            @Override
            public void authenticate(String username, char[] password, MAGResultReceiver<JSONObject> resultReceiver) {
                mssoClient.authenticate(username, password, resultReceiver);
            }

            @Override
            public void setMobileSsoListener(MobileSsoListener mobileSsoListener) {
                ConfigurationManager.getInstance().setMobileSsoListener(mobileSsoListener);
            }

            @Override
            public void processPendingRequests() {
                mssoClient.processPendingRequests();
            }

            public void cancelRequest(long requestId) {
                mssoClient.cancelRequest(requestId);
            }

            @Override
            public void cancelAllRequests() {
                mssoClient.cancelAll();
            }
        };
    }

}
