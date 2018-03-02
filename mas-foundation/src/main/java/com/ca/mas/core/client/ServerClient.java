/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.client;

import android.util.Log;

import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Utility class that encapsulates talking to the token server into Java method calls.
 * This handles just the network protocol for communicating with the token server to register a device, obtain an access token, etc.
 * It does not deal with state management, token persistence, looking up credentials in the context, or anything other
 * higher-level issue.
 */

public abstract class ServerClient {

    public static final String X_CA_ERR = "x-ca-err";
    public static final String DEFAULT_CONTENT_TYPE = " text/plain";
    public static final String UTF_8 = "utf-8";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String OPENID = "openid";
    public static final String MSSO = "msso";
    public static final String MSSO_REGISTER = "msso_register";
    public static final String MSSO_CLIENT_REGISTER = "msso_client_register";
    public static final String MAG_IDENTIFIER = "mag-identifier";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String GRANT_TYPE = "grant_type";
    public static final String ASSERTION = "assertion";
    public static final String SCOPE = "scope";
    public static final String OPENID_PHONE_EMAIL = "openid phone email";
    public static final String DEVICE_ID = "device-id";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String NONCE = "nonce";
    public static final String CLIENT_EXPIRATION = "client_expiration";
    public static final String CLIENT_AUTHORIZATION = "client-authorization";
    public static final String DEVICE_NAME = "device-name";
    public static final String CREATE_SESSION = "create-session";
    public static final String CERT_FORMAT = "cert-format";
    public static final String PEM = "pem";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String DEVICE_STATUS = "device-status";
    public static final String ACTIVATED = "activated";
    public static final String REGISTERED = "registered";
    public static final String ID_TOKEN = "id-token";
    public static final String ID_TOKEN_TYPE = "id-token-type";

    protected final MssoContext mssoContext;

    public ServerClient(MssoContext mssoContext) {
        if (mssoContext == null)
            throw new NullPointerException("mssoContext");
        this.mssoContext = mssoContext;
        this.conf = mssoContext.getConfigurationProvider();
    }

    protected final ConfigurationProvider conf;

    public static <T extends MAGServerException> T createServerException(MASResponse response, Class<T> c) {

        try {
            int errorCode = findErrorCode(response);
            MASResponseBody body = response.getBody();
            String contentType = DEFAULT_CONTENT_TYPE;
            String message = "";
            if (body != null) {
                contentType = body.getContentType();
                byte[] content = body.getRawContent();
                if (content != null) {
                    message = new String(content);
                }
            }
            int statusCode = response.getResponseCode();
            Constructor constructor = c.getConstructor(int.class, int.class, String.class, String.class);
            return (T) constructor.newInstance(errorCode, statusCode, contentType, message);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }


    }

    public static int findErrorCode(MASResponse response) {
        if (response.getResponseCode() != HttpURLConnection.HTTP_BAD_METHOD) {

            Map<String, List<String>> headers = response.getHeaders();

            if (headers != null) {
                List<String> errorCodes = headers.get(X_CA_ERR);
                if (errorCodes == null || errorCodes.size() == 0) {
                    return -1;
                }
                return Integer.parseInt(errorCodes.get(0));
            }
        }
        return -1;
    }

    protected ServerResponse obtainServerResponseToPostedForm(MASRequest request) throws OAuthException, OAuthServerException {

        return obtainServerResponse(request);
    }

    private ServerResponse obtainServerResponse(MASRequest request) throws OAuthException, OAuthServerException {
        MAGHttpClient httpClient = mssoContext.getMAGHttpClient();

        final MASResponse<String> response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new OAuthException(MAGErrorCode.UNKNOWN, "Unable to post to " + request.getURL() + ": " + e.getMessage(), e);
        }

        if (DEBUG) Log.d(TAG,
                String.format("%s response with status: %d",
                        request.getURL(),
                        response.getResponseCode()) );

        try {
            final int statusCode = response.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw ServerClient.createServerException(response, OAuthServerException.class);
            }

            MASResponseBody<String> responseEntity = response.getBody();
            if (responseEntity == null)
                throw new OAuthException(MAGErrorCode.UNKNOWN, "Response from " + request.getURL() + " did not contain an entity");


            final String responseString = responseEntity.getContent();

            if (responseString == null) {
                throw new OAuthException(MAGErrorCode.UNKNOWN, "response from " + request.getURL() + " was empty, with status=" + statusCode);
            }
            return new ServerResponse(statusCode, responseString);

        } catch (JSONException e) {
            throw new OAuthException(MAGErrorCode.UNKNOWN, "response from " + request.getURL() + " was not valid JSON: " + e.getMessage(), e);
        }
    }

}
