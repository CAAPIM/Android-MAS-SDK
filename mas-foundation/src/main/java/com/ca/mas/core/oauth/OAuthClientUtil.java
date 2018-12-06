/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */


package com.ca.mas.core.oauth;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.OAuthTokenContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.foundation.MAS.DEBUG;

public class OAuthClientUtil {

    private OAuthClientUtil() {
    }

    public static PKCE generateCodeChallenge() {
        int encodeFlags = Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE;
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String codeVerifier = Base64.encodeToString(randomBytes, encodeFlags);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(codeVerifier.getBytes("ISO_8859_1"));
            byte[] digestBytes = messageDigest.digest();
            return new PKCE("S256", Base64.encodeToString(digestBytes, encodeFlags), codeVerifier);
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) Log.w("SHA-256 not supported", e);
            return new PKCE("plain", codeVerifier, codeVerifier);
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) Log.e("PKCE not supported", e.getMessage(), e);
            return null;
        }
    }

    public static MASRequest getLogoutRequest() {

        TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();

        IdToken idToken = tokenManager.getIdToken();
        if (idToken != null) {
            URI uri = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT);

            final List<Pair<String, String>> form = new ArrayList<>();
            form.add(new Pair<>(OAuthClient.ID_TOKEN, tokenManager.getIdToken().getValue()));
            form.add(new Pair<>(OAuthClient.ID_TOKEN_TYPE, tokenManager.getIdToken().getType()));
            form.add(new Pair<>(OAuthClient.LOGOUT_APPS, Boolean.toString(true)));

            return new MASRequest.MASRequestBuilder(uri)
                    .post(MASRequestBody.urlEncodedFormBody(form))
                    .responseBody(MASResponseBody.stringBody())
                    .connectionListener(new MASConnectionListener() {
                        @Override
                        public void onObtained(HttpURLConnection connection) {
                            // Inject the client credential during runtime instead of compiling the request,
                            // For SSO scenario, the user has login but the client credential is empty
                            ClientCredentialContainer container = StorageProvider.getInstance().getClientCredentialContainer();
                            String clientId = container.getClientId();
                            String clientSecret = container.getClientSecret();

                            String header = "Basic " + IoUtils.base64(clientId + ":" + clientSecret, Charsets.ASCII);
                            connection.setRequestProperty(OAuthClient.AUTHORIZATION, header);
                        }

                        @Override
                        public void onConnected(HttpURLConnection connection) {
                            //ignore
                        }
                    })
                    .build();
        } else {
            return null;
        }
    }

    public static MASRequest getRevokeRequest() {

        OAuthTokenContainer tokenContainer = StorageProvider.getInstance().getOAuthTokenContainer();

        String refreshToken = tokenContainer.getRefreshToken();
        if (refreshToken != null) {
            String endpointPath = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.REVOKE_ENDPOINT);

            Uri.Builder uriBuilder = new Uri.Builder().encodedPath(endpointPath);
            uriBuilder.appendQueryParameter(OAuthClient.TOKEN, tokenContainer.getRefreshToken())
                    .appendQueryParameter(OAuthClient.TOKEN_TYPE, "refresh_token");
            Uri uri = uriBuilder.build();

            ClientCredentialContainer container = StorageProvider.getInstance().getClientCredentialContainer();

            return new MASRequest.MASRequestBuilder(uri)
                    .delete(null)
                    .responseBody(MASResponseBody.stringBody())
                    .header(OAuthClient.AUTHORIZATION, "Basic " + IoUtils.base64(container.getClientId() + ":" + container.getClientSecret(), Charsets.ASCII))
                    .build();
        } else {
            return null;
        }
    }


}
