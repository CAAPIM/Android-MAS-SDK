/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.ent.BrowserAppNotExistException;
import com.ca.mas.core.ent.EnterpriseBrowserException;
import com.ca.mas.core.ent.InvalidURLException;
import com.ca.mas.core.ent.NativeAppNotExistException;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.MssoIntents;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;

public class App implements Parcelable {
    private static final String TAG = App.class.getCanonicalName();
    private String id;
    private String name;
    private String iconUrl;
    private String authUrl;
    private String nativeUri;
    private String custom;

    public App(JSONObject app) {
        try {
            this.id = app.getString("id");
            this.name = app.optString("name");
            this.iconUrl = app.optString("icon_url");
            this.authUrl = app.optString("auth_url");
            this.nativeUri = app.optString("native_url");
            this.custom = app.optString("custom");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid Enterprise App Configure.", e);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getNativeUri() {
        return nativeUri;
    }

    public JSONObject getCustom() {
        if (custom != null) {
            try {
                return new JSONObject(custom);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Renders the app icon. The app icon will be downloaded with the provided URL
     * and cache into memory for performance improvement.
     * {@link View.OnClickListener} will be added to the Image view with the following action:<br>
     * <ul>
     * <li>For native applications, an Intent will send with Action Intent.ACTION_DEFAULT and the provided native URI using
     * {@link Intent#Intent(String, Uri)}<br>.
     * <li>For web applications, an Intent will send with Action {@link com.ca.mas.core.service.MssoIntents#ACTION_RENDER_WEBVIEW}
     * and the Extra will include the App object with {@link com.ca.mas.core.service.MssoIntents#EXTRA_APP}.
     * </ul>
     *
     * @param context      The Application Context
     * @param appIcon      The ImageView for the app Icon
     * @param errorHandler To handle any error during icon rendering. The error code are defined under
     *                     {@link com.ca.mas.core.service.MssoIntents RESULT_CODE_ERR_ENTERPRISE_BROWSER*}
     */
    public void renderIcon(final Context context, final ImageView appIcon, final ResultReceiver errorHandler) {
        new ImageFetcher(appIcon).execute(iconUrl);
        appIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nativeUri != null && nativeUri.length() > 0) {
                    Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(nativeUri));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (isIntentSafe(context, intent)) {
                        context.startActivity(intent);
                        return;
                    } else {

                        sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_NATIVE_APP_NOT_EXIST,
                                new NativeAppNotExistException("Native app does not exist"));
                    }
                }

                if (authUrl != null && authUrl.length() > 0) {
                    Intent intent = new Intent(MssoIntents.ACTION_RENDER_WEBVIEW);
                    intent.putExtra(MssoIntents.EXTRA_APP, App.this);
                    intent.setPackage(context.getPackageName());
                    context.startActivity(intent);
                } else {
                    sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_APP_NOT_EXIST,
                            new BrowserAppNotExistException("Browser App does not exist"));
                }
            }
        });
    }

    private boolean isIntentSafe(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    /**
     * Renders the WebView for the Application Auth URL.
     * A {@link WebViewClient} will be added to the WebView to intercept the request to the gateway for
     * authentication. Once authentication is completed, the result from the Web Application will be rendered in
     * the provided webview.
     *
     * @param context      The Application Context
     * @param webView      The WebView to render the result
     * @param errorHandler To handle any error during the authentication. The error codes are defined under
     *                     {@link com.ca.mas.core.service.MssoIntents RESULT_CODE_ERR_ENTERPRISE_BROWSER*}
     */
    public void renderWebView(final Context context, final WebView webView, final ResultReceiver errorHandler) {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(getWebViewClient(context, errorHandler));

        final MobileSso mobileSso = MobileSsoFactory.getInstance();
        try {
            if (!(new URL(authUrl)).getHost().equals(mobileSso.getConfigurationProvider().getTokenHost())) {
                sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL,
                        new InvalidURLException("This auth url is valid only for the host that has issued the access_token"));
                return;
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
            sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL, new InvalidURLException(e));
            return;
        }

        webView.loadUrl(authUrl);
    }

    /**
     * Retrieves the default implementation of how the WebView will receive various notifications and requests.
     * The default WebViewClient handles all the handshakes with the MAG by intercepting the request through
     * {@link WebViewClient#shouldInterceptRequest(WebView, String)}, and prompts a
     * dialog for user consent when accessing a self signed URL. Developers can change the default behavior by
     * overriding this method and extending the {@link com.ca.mas.core.app.App} class and registers through
     * {@link com.ca.mas.core.EnterpriseApp#processEnterpriseApp(Context, ResultReceiver, Class)}
     *
     * @param context      Application Context
     * @param errorHandler To handle any error during the authentication. The error codes are defined under
     *                     {@link com.ca.mas.core.service.MssoIntents RESULT_CODE_ERR_ENTERPRISE_BROWSER*}
     * @return The WebViewClient that will receive various notifications and requests.
     */
    protected WebViewClient getWebViewClient(final Context context, final ResultReceiver errorHandler) {
        final MobileSso mobileSso = MobileSsoFactory.getInstance();

        return new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                onSslError(context, handler, error);
            }

            /**
             * Sends the error to the registered ResultReceiver.
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL,
                        new InvalidURLException("Error Code from WebView: " + errorCode));
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return this.shouldInterceptRequest(view, request.getUrl().toString());
            }

            // Keep this to support version < Build.VERSION_CODES.LOLLIPOP
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
                if (url.equals(authUrl)) {
                    final CountDownLatch latch = new CountDownLatch(1);
                    final WebResourceResponse webResourceResponse = new WebResourceResponse(null, null, null);
                    MAGRequest request = null;
                    try {
                        request = new MAGRequest.MAGRequestBuilder(new URL(url)).build();
                    } catch (MalformedURLException e) {
                        sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL,
                                new InvalidURLException(e));
                    }

                    mobileSso.processRequest(request, new MAGResultReceiver<byte[]>() {
                        @Override
                        public void onSuccess(MAGResponse<byte[]> response) {
                            try {
                                webResourceResponse.setData(new ByteArrayInputStream(response.getBody().getContent()));
                                webResourceResponse.setEncoding("UTF-8");
                                webResourceResponse.setMimeType("text/html");
                            } catch (Exception e) {
                                sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL, new InvalidURLException(e));
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onError(MAGError error) {
                            Log.e(TAG, "Failed to retrieve response content from URL:" + authUrl, error.getCause());
                            sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL, new InvalidURLException(error));
                            latch.countDown();
                        }

                        @Override
                        public void onRequestCancelled() {
                            latch.countDown();
                        }
                    });

                    try {
                        latch.await(300, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        String message = "Auth failed with URL: " + authUrl;
                        Log.e(TAG, message, e);
                        sendError(errorHandler, MssoIntents.RESULT_CODE_ERR_ENTERPRISE_BROWSER_INVALID_URL, new InvalidURLException(e));
                    }
                    return webResourceResponse;
                } else {
                    // Don't intercept the request.
                    return null;
                }
            }
        };
    }

    private void sendError(ResultReceiver handler, int code, EnterpriseBrowserException e) {
        if (handler != null) {
            Bundle result = new Bundle();
            result.putSerializable(MssoIntents.RESULT_ERROR, e);
            result.putString(MssoIntents.RESULT_ERROR_MESSAGE, e.getMessage());
            handler.send(code, result);
        }
    }

    private void sendError(ResultReceiver handler, int code, Bundle bundle) {
        if (handler != null) {
            handler.send(code, bundle);
        }
    }

    /**
     * Notify the host application that an SSL error occurred while loading a
     * resource. The host application must call either handler.cancel() or
     * handler.proceed().
     * <br>
     * A Default dialog will prompt for user consents to proceed or not to proceed.
     *
     * @param context The application Context.
     * @param handler An SslErrorHandler object that will handle the user's response.
     * @param error   The SSL error object.
     */
    protected void onSslError(final Context context, final SslErrorHandler handler, final SslError error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog ad = builder.create();
        String message = null;
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                message = "Certificate is untrusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = "Certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = "Certificate ID is mismatched.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = "Certificate is not yet valid.";
                break;
            case SslError.SSL_DATE_INVALID:
                message = "Certificate date is invalid .";
                break;
            default:
                message = "Certificate is invalid .";
                break;
        }
        message += " Do you want to continue anyway?";
        ad.setTitle("SSL Certificate Error");
        ad.setMessage(message);
        ad.setButton(BUTTON_POSITIVE, "OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        ad.setButton(BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
                ((Activity) context).finish();
            }
        });
        ad.show();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconUrl);
        dest.writeString(this.authUrl);
        dest.writeString(this.nativeUri);
        dest.writeString(this.custom);
    }

    protected App(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.iconUrl = in.readString();
        this.authUrl = in.readString();
        this.nativeUri = in.readString();
        this.custom = in.readString();
    }

    public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>() {
        @Override
        public App createFromParcel(Parcel source) {
            return new App(source);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
