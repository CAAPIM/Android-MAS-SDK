package com.ca.mas.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASSocialLogin;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public class CustomTabs {
    public static void socialLogin(final Context context, MASAuthenticationProvider provider) {
        MASSocialLogin.getAuthConfiguration(context, provider, new MASCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String clientId = uri.getQueryParameter("client_id");
                String redirectUri = uri.getQueryParameter("redirect_uri");
                String scope = uri.getQueryParameter("scope");
                String state = uri.getQueryParameter("state");
                String responseType = uri.getQueryParameter("response_type");

                try {
                    URL url = new URL(uri.toString());
                    String configuration = url.getProtocol() + "://" + url.getAuthority() + url.getPath();
                    Uri authEndpoint = Uri.parse(configuration);
                    Uri tokenEndpoint = Uri.parse("");
                    AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                            authEndpoint, tokenEndpoint, null);

                    AuthorizationRequest req = new AuthorizationRequest.Builder(
                            config, clientId, responseType, Uri.parse(redirectUri))
                            .setState(state)
                            .setScopes(scope)
                            .setCodeVerifier(null)
                            .build();

                    Context context = MAS.getContext();
                    Intent postAuthIntent = new Intent(context, MASOAuthRedirectActivity.class);
                    Intent authCanceledIntent = new Intent(context, Empty.class);

                    AuthorizationService service = new AuthorizationService(MAS.getContext());
                    service.performAuthorizationRequest(req,
                            PendingIntent.getActivity(context, req.hashCode(), postAuthIntent, 0),
                            PendingIntent.getActivity(context, req.hashCode(), authCanceledIntent, 0));
                } catch (MalformedURLException e) {
                    Log.d("", e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d("CustomTabs", e.getMessage());
            }
        });
    }
}
