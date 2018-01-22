/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.core.conf.ConfigurationManager;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An Http Api Request. Instances of this class are immutable.
 */
public interface MASRequest {

    enum Method {GET, PUT, POST, DELETE}

    /**
     * @return URL of this request
     */
    URL getURL();

    /**
     * @return Request method of this request. Please refer to
     * {@link Method} for supporting method
     */
    String getMethod();

    /**
     * @return A list of HTTP headers for this request
     */
    Map<String, List<String>> getHeaders();

    /**
     * @return The grant provider for this request. Please refer to {@link MASGrantProvider} for
     * supporting grant provider.
     */
    MASGrantProvider getGrantProvider();

    /**
     * @return The body to be sent with this request
     */
    MASRequestBody getBody();

    /**
     * @return The {@link HttpURLConnection} listener.
     */
    MASConnectionListener getConnectionListener();

    /**
     * @return The response body for this request. The default response body is set to
     * {@link MASResponseBody#byteArrayBody()}, you can change the response body to
     * {@link MASResponseBody#jsonBody()}, and {@link MASResponseBody#stringBody()} or you can
     * implement your own type of response body type.
     */
    MASResponseBody<?> getResponseBody();

    /**
     * @return The scope for this request. When return null, default scope set (defined in the
     * configuration) will be used.
     */
    String getScope();

    /**
     * @return whether the request is being made outside of primary gateway.
     * When the value is set to true, all automatically injected credentials in SDK will be excluded in the request.
     */
    boolean isPublic();

    /**
     * Notify the {@link MASCallback#onError(Throwable)} when the request is cancelled by {@link MAS#cancelRequest(long)}.
     *
     * @return True to invoke {@link MASCallback#onError(Throwable)} with {@link com.ca.mas.foundation.MAS.RequestCancelledException} when the request is cancelled by
     * {@link MAS#cancelRequest(long)}. Default is false.
     */
    boolean notifyOnCancel();

    /**
     * Builder class to build {@link MASRequest} object
     */
    class MASRequestBuilder {

        private URL url;
        private String method = Method.GET.name();
        private Map<String, List<String>> headers = new HashMap<>();
        private MASRequestBody body;
        private MASResponseBody<?> responseBody = new MASResponseBody();
        private MASGrantProvider grantProvider = ConfigurationManager.getInstance().getDefaultGrantProvider();
        private String scope;
        private MASConnectionListener listener;
        private boolean isPublic;
        private long timeout;
        private TimeUnit timeUnit;

        private boolean notifyOnCancel = false;
        private boolean sign = false;
        private MASClaims claim;
        private PrivateKey privateKey;

        /**
         * Create a builder with the provided {@link URI}.
         *
         * @param uri the provided URI
         */
        public MASRequestBuilder(URI uri) {
            if (uri != null) {
                try {
                    this.url = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(uri.toString()).toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        /**
         * Create a builder with the provided {@link Uri}
         *
         * @param uri the provided Uri
         */
        public MASRequestBuilder(Uri uri) {

            if (uri != null) {
                try {
                    this.url = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(uri.toString()).toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        /**
         * Create a build with provided {@link URL}
         *
         * @param url the provided URL
         */

        public MASRequestBuilder(URL url) {
            if (url != null) {
                try {
                    this.url = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(url.toString()).toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        /**
         * Convenience method for reconstructing a request as a builder
         *
         * @param request
         */
        public MASRequestBuilder(MASRequest request) {
            this.method = request.getMethod();
            this.body = request.getBody();
            this.grantProvider = request.getGrantProvider();
            this.scope = request.getScope();
            this.responseBody = request.getResponseBody();
            this.isPublic = request.isPublic();
            this.headers = request.getHeaders();
            this.listener = request.getConnectionListener();
        }


        /**
         * Sets the request method to GET.
         *
         * @return The builder
         */
        public MASRequestBuilder get() {
            this.method = Method.GET.name();
            this.body = null;
            return this;
        }

        /**
         * Sets the request method to POST with {@link MASRequestBody}
         *
         * @param body The MAGRequestBody to POST with.
         * @return The builder
         */
        public MASRequestBuilder post(MASRequestBody body) {
            this.method = Method.POST.name();
            this.body = body;
            return this;
        }

        /**
         * Sets the request method to PUT with {@link MASRequestBody}
         *
         * @param body The MAGRequestBody to PUT with.
         * @return The builder
         */
        public MASRequestBuilder put(MASRequestBody body) {
            this.method = Method.PUT.name();
            this.body = body;
            return this;
        }

        /**
         * Sets the request method to DELETE with {@link MASRequestBody},
         * set {@link MASRequestBody} to null if not request body to send.
         *
         * @param body The MAGRequestBody to delete.
         * @return The builder
         */
        public MASRequestBuilder delete(MASRequestBody body) {
            this.method = Method.DELETE.name();
            this.body = body;
            return this;
        }

        /**
         * Sets request grant type to {@link MASGrantProvider#PASSWORD}
         *
         * @return The builder
         */
        public MASRequestBuilder password() {
            this.grantProvider = MASGrantProvider.PASSWORD;
            return this;
        }

        /**
         * Sets request grant type to {@link MASGrantProvider#CLIENT_CREDENTIALS}
         *
         * @return The builder
         */
        public MASRequestBuilder clientCredential() {
            this.grantProvider = MASGrantProvider.CLIENT_CREDENTIALS;
            return this;
        }

        /**
         * Sets the request grant scope; use a space delimiter for more than one scope, e.g. for READ WRITE.
         *
         * @param scope The scope.
         * @return The builder
         */
        public MASRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets the type of parsed response. To specify the response, the SDK converts the correct
         * data type for the response. The default response type is {@link MASResponseBody#byteArrayBody()}.
         *
         * @param responseBody The response body.
         * @return The builder
         */
        public MASRequestBuilder responseBody(MASResponseBody responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        /**
         * The request is being made outside of primary gateway.
         * When the public attribute is set, all automatically injected credentials in SDK will be excluded in the request.
         *
         * @return The builder
         */
        public MASRequestBuilder setPublic() {
            this.isPublic = true;
            return this;
        }

        /**
         * Adds the specified header to the request.
         *
         * @param name  Header name
         * @param value Header value
         * @return The builder
         */
        public MASRequestBuilder header(String name, String value) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<>();
                headers.put(name, values);
            }
            values.add(value);
            return this;
        }

        /**
         * Removes the specified header from the request.
         *
         * @param name Header name
         * @return The builder
         */
        public MASRequestBuilder removeHeader(String name) {
            headers.remove(name);
            return this;
        }

        /**
         * <p>Register {@link HttpURLConnection} listener.</p>
         * The SDK uses Android {@link HttpURLConnection} to connect to the API; developers can
         * intercept the connection with {@link MASConnectionListener}
         * before a request is sent to the backend service.
         *
         * @param listener The connection listener
         * @return The builder
         */
        public MASRequestBuilder connectionListener(MASConnectionListener listener) {
            this.listener = listener;
            return this;
        }

        public MASRequestBuilder notifyOnCancel() {
            this.notifyOnCancel = true;
            return this;
        }

        /**
         * Signs the request with the device registered private key and injects JWT claims based on the user information.
         * This method will use a default value of 5 minutes for the JWS 'exp' claim.
         *
         * @return The builder
         */
        public MASRequestBuilder sign() {
            this.sign = true;
            return this;
        }

        /**
         * Signs the request with the device registered private key and injects JWT claims based on the user information.
         * This method will use a default value of 5 minutes for the JWS 'exp' claim if not provided.
         *
         * @return The builder
         */
        public MASRequestBuilder sign(MASClaims claim) {
            this.sign = true;
            this.claim = claim;
            return this;
        }

        /**
         * Signs the request with the provided private key and injects JWT claims based on the user information.
         *
         * @return The builder
         */
        public MASRequestBuilder sign(PrivateKey privateKey) {
            this.sign = true;
            this.privateKey = privateKey;
            return this;
        }

        /**
         * Builds the {@link MASRequest} object.
         *
         * @return An immutable {@link MASRequest} object.
         */
        public MASRequest build() {
            //Add the headers
            Map<String, List<String>> newHeaders = new HashMap<>();
            for (String key : headers.keySet()) {
                List<String> headerValues = new ArrayList<>();
                if (headers.get(key) != null) {
                    for (String value : headers.get(key)) {
                        headerValues.add(value);
                    }
                }
                newHeaders.put(key, Collections.unmodifiableList(headerValues));
            }

            final Map<String, List<String>> unmodifiableHeaders = Collections.unmodifiableMap(newHeaders);

            //If isPublic() is false, we check the security configuration and match its isPublic() setting
            if (!isPublic && url != null) {
                Uri uri = Uri.parse(url.toString());
                MASSecurityConfiguration config = MASConfiguration.getCurrentConfiguration().getSecurityConfiguration(uri);
                if (config != null && config.isPublic()) {
                    setPublic();
                }
            }

            return new MASRequest() {

                @Override
                public URL getURL() {
                    return url;
                }

                @Override
                public String getMethod() {
                    return method;
                }

                @Override
                public Map<String, List<String>> getHeaders() {
                    return unmodifiableHeaders;
                }

                @Override
                public MASGrantProvider getGrantProvider() {
                    return grantProvider;
                }

                @Override
                public MASConnectionListener getConnectionListener() {
                    return listener;
                }

                @Override
                public String getScope() {
                    return scope;
                }

                @Override
                public boolean isPublic() {
                    return isPublic;
                }

                @Override
                public boolean notifyOnCancel() {
                    return notifyOnCancel;
                }

                @Override
                public MASRequestBody getBody() {
                    if (sign && body != null) {
                        return MASRequestBody.jwtClaimsBody(claim, privateKey, body);
                    } else {
                        return body;
                    }
                }

                @Override
                public MASResponseBody<?> getResponseBody() {
                    return responseBody;
                }


            };
        }
    }

}
