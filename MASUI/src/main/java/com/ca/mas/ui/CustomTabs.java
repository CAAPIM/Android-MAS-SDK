package com.ca.mas.ui;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.io.ssl.MAGSocketFactory;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASSocialLogin;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.connectivity.ConnectionBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class CustomTabs {

    public static void socialLogin(final Context context, MASAuthenticationProvider provider){
        MASSocialLogin.getAuthConfiguration(context, provider, new MASCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    URL url = new URL(uri.toString());
                    Map<String, List<String>> query = splitQuery(url);

                    String configuration = url.getProtocol() + "://" + url.getAuthority() + url.getPath();
                    String clientId = query.get("client_id").get(0);
                    String redirectUri = query.get("redirect_uri").get(0);
                    String state = query.get("state").get(0);
                    String responseType = query.get("response_type").get(0);
                    String[] scope = query.get("scope").get(0).split("\\s+");

                    AppAuthConfiguration appAuthConfiguration = new AppAuthConfiguration.Builder()
                            .setConnectionBuilder(new ConnectionBuilder() {
                                @NonNull
                                @Override
                                public HttpURLConnection openConnection(@NonNull Uri uri) throws IOException {
                                    URL url = new URL(uri.toString());
                                    HttpURLConnection connection =
                                            (HttpURLConnection) url.openConnection();
                                    if (connection instanceof HttpsURLConnection) {
                                        ((HttpsURLConnection) connection).setSSLSocketFactory(
                                                new MAGSocketFactory(context).createSSLSocketFactory());
                                    }

                                    return connection;
                                }
                            })
                            .build();


                    AuthorizationServiceConfiguration config =
                            new AuthorizationServiceConfiguration(Uri.parse(configuration),
                                    Uri.parse("https://login.salesforce.com/services/oauth2/token"), null);

                    AuthorizationRequest req = new AuthorizationRequest.Builder(
                            config,
                            clientId,
                            responseType,
                            Uri.parse(redirectUri))
                            .setState(state)
                            .setScopes(scope)
                            .setCodeVerifier(null)
                            .build();

                    Intent postAuthIntent = new Intent(context, Empty.class);
                    Intent authCancelledIntent = new Intent(context, Empty.class);

                    AuthorizationService service = new AuthorizationService(context, appAuthConfiguration);
                    service.performAuthorizationRequest(req, PendingIntent.getActivity(context,
                            req.hashCode(), postAuthIntent, 0),
                            PendingIntent.getActivity(context, req.hashCode(), authCancelledIntent, 0));
                } catch (UnsupportedEncodingException | MalformedURLException e) {
                    Log.d("", e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d("CustomTabs", e.getMessage());
            }
        });
    }

    private static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
