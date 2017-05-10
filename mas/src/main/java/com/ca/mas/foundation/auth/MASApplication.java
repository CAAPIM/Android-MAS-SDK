/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.app.App;
import com.ca.mas.core.app.ImageFetcher;
import com.ca.mas.core.ent.InvalidResponseException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.foundation.MAS.DEBUG;

/**
 * The Enterprise browser manages a trusted group of enterprise approved applications on a device.
 * Applications within this trusted group can be accessed through single sign-on.
 */
public abstract class MASApplication {
    private MASApplication() {
    }

    private static String TAG = MASApplication.class.getCanonicalName();
    private static MASApplicationLauncher mLauncher = new MASApplicationLauncher() {
        @Override
        public void onWebAppLaunch(MASApplication application) {

        }
    };

    /**
     * @return The application identifier.
     */
    public abstract String getIdentifier();

    /**
     * @return The name of the app.
     */
    public abstract String getName();

    /**
     * @return The path to the icon graphic displayed on the device. For example: http://server/connect/enterprise/browser/icon/appa
     */
    public abstract String getIconUrl();

    /**
     * @return The path to the MAG endpoint to request a SAML token. For example: /connect/enterprise/browser/websso/authorize/appc
     * Used when the application is not installed on the device. The application is accessed through {@link android.webkit.WebView}.
     * Construct {@link MASWebApplication} with the {@link android.webkit.WebView} and the AuthUrl
     */
    public abstract String getAuthUrl();

    /**
     * @return The path to launch the native application.
     * This matches the path declared by the app developer in the format: schemeValue://hostValue,
     * for example:  camsso://com.camsso.myappname
     */
    public abstract String getNativeUri();

    /**
     * @return Includes optional custom-key and custom-value variables that provide additional data for the requesting app.
     * This section is ignored by the SDK and can be empty;
     */
    public abstract JSONObject getCustom();

    /**
     * Set the Application Launcher to launch the Native App and Web App, {@link MASApplicationLauncher#onNativeAppLaunch(MASApplication)}
     * is triggered for Native App and {@link MASApplicationLauncher#onWebAppLaunch(MASApplication)} for Web App.
     */
    public static void setApplicationLauncher(MASApplicationLauncher launcher) {
        mLauncher = launcher;
    }

    /**
     * The application icon is downloaded with the provided URL from {@link #getIconUrl()}
     * and cached into memory for performance improvement.
     *
     * @param imageView A ImageView for the App Icon
     */
    public abstract void renderEnterpriseIcon(ImageView imageView);

    /**
     * The MAG administrator creates a JSON-formatted list of native or non-native applications.
     * Native applications are created with the SDK, registered with the MAS, and installed on the device.
     * Non-native applications are not installed on the device but accessed through webView.
     * For approved non-native applications, webSSO with SAML based federation is used.
     *
     * @param callback Callback to receive a list of native or non-native applications
     */
    public static void retrieveEnterpriseApps(final MASCallback<List<MASApplication>> callback) {
        String path = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_ENTERPRISE_APPS);

        try {
            MASRequest r = new MASRequest.MASRequestBuilder(new URI(path)).build();
            MAS.invoke(r, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {

                    try {
                        List<MASApplication> applications = new ArrayList<MASApplication>();
                        JSONArray apps = result.getBody().getContent().optJSONArray("enterprise-apps");
                        if (apps != null) {
                            for (int i = 0; i < apps.length(); i++) {
                                final App a = new App(((JSONObject) apps.get(i)).getJSONObject("app"));
                                applications.add(new MASApplication() {
                                    @Override
                                    public String getIdentifier() {
                                        return a.getId();
                                    }

                                    @Override
                                    public String getName() {
                                        return a.getName();
                                    }

                                    @Override
                                    public String getIconUrl() {
                                        return a.getIconUrl();
                                    }

                                    @Override
                                    public String getAuthUrl() {
                                        return a.getAuthUrl();
                                    }

                                    @Override
                                    public String getNativeUri() {
                                        return a.getNativeUri();
                                    }

                                    @Override
                                    public JSONObject getCustom() {
                                        return a.getCustom();
                                    }

                                    @Override
                                    public void renderEnterpriseIcon(ImageView imageView) {
                                        new ImageFetcher(imageView).execute(getIconUrl());
                                        final MASApplication t = this;
                                        imageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (getNativeUri() != null && getNativeUri().length() > 0) {
                                                    if (mLauncher != null) {
                                                        mLauncher.onNativeAppLaunch(t);
                                                    }
                                                    return;
                                                }

                                                if (getAuthUrl() != null && getAuthUrl().length() > 0) {
                                                    if (mLauncher != null) {
                                                        mLauncher.onWebAppLaunch(t);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        Callback.onSuccess(callback, applications);
                    } catch (Exception e) {
                        if (e.getCause() != null && e.getCause() instanceof JSONException) {
                            Callback.onError(callback, new InvalidResponseException(e.getCause()));
                            return;
                        }
                        Callback.onError(callback, e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }
    }

    public static abstract class MASApplicationLauncher {
        /**
         * Notify the host application of a request to launch the native app.
         */
        public void onNativeAppLaunch(MASApplication application) {
            Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(application.getNativeUri()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                MAS.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (DEBUG) Log.e(TAG, e.getMessage(), e);
                throw e;
            }
        }

        /**
         * Notify the host application of a request to launch the web app.
         */
        public abstract void onWebAppLaunch(MASApplication application);
    }
}
