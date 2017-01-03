/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.Provider;
import com.ca.mas.core.token.IdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ca.mas.core.MAG.DEBUG;

/**
 * Utility class that encapsulates talking to the token server into Java method calls.
 * This handles just the network protocol for communicating with the MAG server to authorize request .
 * It does not deal with state management, token persistence, looking up credentials in the context, or anything other
 * higher-level issue.
 */
public class OAuthClient extends ServerClient {

    private static final int INVALID_CLIENT_CREDENTIALS = 3000201;

    private static final String DEFAULT_DISPLAY = "social_login";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CODE = "code";
    public static final String DISPLAY = "display";
    public static final String MSSO_REGISTER = "msso_register";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String IDP = "idp";
    public static final String PROVIDERS = "providers";
    public static final String PROVIDER = "provider";
    public static final String ID = "id";
    public static final String AUTH_URL = "auth_url";
    public static final String POLL_URL = "poll_url";
    public static final String ID_TOKEN = "id_token";
    public static final String ID_TOKEN_TYPE = "id_token_type";
    public static final String LOGOUT_APPS = "logout_apps";
    public static final String AUTHORIZATION = "authorization";
    public static final String CODE_CHALLENGE = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    public static final String STATE = "state";

    public OAuthClient(MssoContext mssoContext) {
        super(mssoContext);
    }

    /**
     * Retrieve the supported Social Login provider from the Gateway if redirect uri is defined in the configuration.
     *
     * @param context The MssoContext
     * @return List of Social Login Provider. Never return null.
     */
    public AuthenticationProvider getSocialPlatformProvider(Context context) throws OAuthException, OAuthServerException {

        ConfigurationProvider config = mssoContext.getConfigurationProvider();
        String redirectUri = config.getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
        String idp = AuthenticationProvider.ENTERPRISE;
        ArrayList<Provider> spProviders = new ArrayList<>();

        if (redirectUri != null) {
            Uri.Builder b = Uri.parse(config.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_AUTHORIZE).toString()).buildUpon();
            b.appendQueryParameter(CLIENT_ID, mssoContext.getClientId());
            b.appendQueryParameter(RESPONSE_TYPE, CODE);

            b.appendQueryParameter(DISPLAY, DEFAULT_DISPLAY);

            Boolean ssoEnabled = config.getProperty(ConfigurationProvider.PROP_SSO_ENABLED);
            if (ssoEnabled == null) {
                ssoEnabled = true;
            }
            String scope = config.getClientScope();
            if (scope == null) {
                scope = "";
            }
            if (ssoEnabled && !scope.contains(OPENID)) {
                scope = scope + " " + OPENID;
            }
            if (!mssoContext.isDeviceRegistered() && !scope.contains(MSSO_REGISTER)) {
                scope = scope + " " + MSSO_REGISTER;
            }
            if (mssoContext.isDeviceRegistered()) {
                scope = scope.replace(MSSO_REGISTER, "");
            }

            if (scope.length() > 0) {
                b.appendQueryParameter(SCOPE, scope.trim());
            }
            b.appendQueryParameter(REDIRECT_URI, redirectUri);

            PKCE pkce = generateCodeChallenge();
            if (pkce != null) {
                b.appendQueryParameter(CODE_CHALLENGE, pkce.codeChallenge);
                b.appendQueryParameter(CODE_CHALLENGE_METHOD, pkce.codeChallengeMethod);
                SecureRandom secureRandom = new SecureRandom();
                byte[] random = new byte[16];
                secureRandom.nextBytes(random);
                String key = Base64.encodeToString(random, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
                CodeVerifierCache.getInstance().store(key, pkce.codeVerifier);
                b.appendQueryParameter(STATE, key);
            }

            MAGHttpClient httpClient = mssoContext.getMAGHttpClient();

            try {
                MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(new URI(b.build().toString()))
                        .responseBody(MAGResponseBody.jsonBody());

                MAGRequest request = builder.build();
                MAGResponse<JSONObject> response = httpClient.execute(request);
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonResponse = response.getBody().getContent();
                    idp = jsonResponse.getString(IDP);
                    JSONArray providers = jsonResponse.getJSONArray(PROVIDERS);
                    for (int i = 0; i < providers.length(); i++) {
                        JSONObject provider = providers.getJSONObject(i).getJSONObject(PROVIDER);
                        String id = provider.getString(ID);
                        String url = provider.getString(AUTH_URL);
                        String poll = provider.optString(POLL_URL);
                        spProviders.add(new Provider(id, url, poll,
                                context.getResources().getIdentifier("drawable/" + id.toLowerCase(), null, context.getPackageName())));
                    }
                } else {
                    int errorCode = findErrorCode(response);
                    if (errorCode == INVALID_CLIENT_CREDENTIALS) {
                        mssoContext.clearClientCredentials();
                    }
                    throw ServerClient.createServerException(response, OAuthServerException.class);
                }

            } catch (IOException e) {
                throw new OAuthException(MAGErrorCode.UNKNOWN, "Unable to retrieve Social Login Providers: " + e.getMessage(), e);
            } catch (JSONException e) {
                throw new OAuthException(MAGErrorCode.UNKNOWN, "response from " + b.toString() + " was not valid response: " + e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new OAuthException(MAGErrorCode.UNKNOWN, e);
            }
        }

        return new AuthenticationProvider(idp, spProviders);

    }

    /**
     * Inform the token server that the specified ID token should be logged out and sessions canceled for
     * all apps using this token
     *
     * @param idToken      an ID token, presumably a JWT.  Required.
     * @param clientId     the client ID of the app seeking access. Required.
     * @param clientSecret the client secret of the app seeking access. Required.
     * @param logoutApps   true if all currently active apps should also be logged out, eg removing all access tokens
     *                     created using this ID token.
     *                     false if currently active access tokens should be allowed to remain valid until they expire.
     * @throws OAuthServerException if there is an error response from the token server
     * @throws OAuthException       if there is an error other than a valid error JSON response from the token server
     */
    public void logout(@NonNull IdToken idToken,
                       @NonNull String clientId,
                       @NonNull String clientSecret, boolean logoutApps) throws OAuthServerException, OAuthException {


        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>(ID_TOKEN, idToken.getValue()));
        form.add(new Pair<String, String>(ID_TOKEN_TYPE, idToken.getType()));
        form.add(new Pair<String, String>(LOGOUT_APPS, Boolean.toString(logoutApps)));


        URI uri = conf.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT);
        MAGRequest request = new MAGRequest.MAGRequestBuilder(uri)
                .post(MAGRequestBody.urlEncodedFormBody(form))
                .responseBody(MAGResponseBody.stringBody())
                .header(AUTHORIZATION, "Basic " + IoUtils.base64(clientId + ":" + clientSecret, Charsets.ASCII))
                .build();

        try {
            obtainServerResponseToPostedForm(request);
        } catch (MAGException e) {
            throw new OAuthException(MAGErrorCode.UNKNOWN, e);
        } catch (MAGServerException e) {
            if (e.getErrorCode() == INVALID_CLIENT_CREDENTIALS) {
                mssoContext.clearClientCredentials();
            }
            throw new OAuthServerException(e);
        }
    }

    private PKCE generateCodeChallenge() {
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

    private static class PKCE {
        String codeChallenge;
        String codeChallengeMethod;
        String codeVerifier;

        public PKCE(String codeChallengeMethod, String codeChallenge, String codeVerifier) {
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.codeVerifier = codeVerifier;
        }
    }
}
