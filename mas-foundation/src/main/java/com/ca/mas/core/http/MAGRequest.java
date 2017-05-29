/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import android.net.Uri;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.oauth.GrantProvider;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An Http Api Request. Instances of this class are immutable.
 */
public interface MAGRequest {
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
     * @return The grant provider for this request. Please refer to {@link GrantProvider} for
     * supporting grant provider.
     */
    GrantProvider getGrantProvider();

    /**
     * @return The body to be sent with this request
     */
    MAGRequestBody getBody();

    /**
     * @return The {@link HttpURLConnection} listener.
     */
    MAGConnectionListener getConnectionListener();

    /**
     * @return The response body for this request. The default response body is set to
     * {@link MAGResponseBody#byteArrayBody()}, you can change the response body to
     * {@link MAGResponseBody#jsonBody()}, and {@link MAGResponseBody#stringBody()} or you can
     * implement your own type of response body type.
     */
    MAGResponseBody<?> getResponseBody();

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

    interface MAGConnectionListener {
        /**
         * Invoke immediately after the call {@link URL#openConnection()}.
         * Note that the connection is not connected and not ready to retrieve any response from
         * the connection.
         *
         * @param connection The HttpURLConnection to the MAG Server
         */
        void onObtained(HttpURLConnection connection);

        /**
         * This method will be invoked after the HTTP request is prepared by the SDK.
         * For POST or PUT, the data has been sent to the connection.
         *
         * @param connection The HttpURLConnection to the MAG Server
         */
        void onConnected(HttpURLConnection connection);
    }

    /**
     * Builder class to build {@link MAGRequest} object
     */
    class MAGRequestBuilder {
        private URL url;
        private String method = Method.GET.name();
        private Map<String, List<String>> headers = new HashMap<>();
        private MAGRequestBody body;
        private MAGResponseBody<?> responseBody = new MAGResponseBody();
        private GrantProvider grantProvider = ConfigurationManager.getInstance().getDefaultGrantProvider();
        private String scope;
        private MAGConnectionListener listener;
        private boolean isPublic = false;
        private boolean sign;
        private long timeout;
        private TimeUnit timeUnit;

        /**
         * Create a builder with the provided {@link URI}.
         *
         * @param uri the provided URI
         */
        public MAGRequestBuilder(URI uri) {
            try {
                if (uri != null) {
                    this.url = uri.toURL();
                }
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Create a builder with the provided {@link Uri}
         *
         * @param uri the provided Uri
         */
        public MAGRequestBuilder(Uri uri) {
            try {
                if (uri != null) {
                    this.url = new URL(uri.toString());
                }
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Create a build with provided {@link URL}
         *
         * @param url the provided URL
         */
        public MAGRequestBuilder(URL url) {
            this.url = url;
        }

        /**
         * Sets the request method to GET.
         *
         * @return The builder
         */
        public MAGRequestBuilder get() {
            this.method = Method.GET.name();
            this.body = null;
            return this;
        }

        /**
         * Sets the request method to POST with {@link MAGRequestBody}
         *
         * @param body The MAGRequestBody to POST with.
         * @return The builder
         */
        public MAGRequestBuilder post(MAGRequestBody body) {
            this.method = Method.POST.name();
            this.body = body;
            return this;
        }

        /**
         * Sets the request method to PUT with {@link MAGRequestBody}
         *
         * @param body The MAGRequestBody to PUT with.
         * @return The builder
         */
        public MAGRequestBuilder put(MAGRequestBody body) {
            this.method = Method.PUT.name();
            this.body = body;
            return this;
        }

        /**
         * Sets the request method to DELETE with {@link MAGRequestBody},
         * set {@link MAGRequestBody} to null if not request body to send.
         *
         * @param body The MAGRequestBody to delete.
         * @return The builder
         */
        public MAGRequestBuilder delete(MAGRequestBody body) {
            this.method = Method.DELETE.name();
            this.body = body;
            return this;
        }

        /**
         * Sets request grant type to {@link GrantProvider#PASSWORD}
         *
         * @return The builder
         */
        public MAGRequestBuilder password() {
            this.grantProvider = GrantProvider.PASSWORD;
            return this;
        }

        /**
         * Sets request grant type to {@link GrantProvider#CLIENT_CREDENTIALS}
         *
         * @return The builder
         */
        public MAGRequestBuilder clientCredential() {
            this.grantProvider = GrantProvider.CLIENT_CREDENTIALS;
            return this;
        }

        /**
         * Sets the request grant scope; use a space delimiter for more than one scope, e.g. for READ WRITE.
         *
         * @param scope The scope.
         * @return The builder
         */
        public MAGRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets the type of parsed response. To specify the response, the SDK converts the correct
         * data type for the response. The default response type is {@link MAGResponseBody#byteArrayBody()}.
         *
         * @param responseBody The response body.
         * @return The builder
         */
        public MAGRequestBuilder responseBody(MAGResponseBody responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        /**
         * The request is being made outside of primary gateway.
         * When the public attribute is set, all automatically injected credentials in SDK will be excluded in the request.
         *
         * @return The builder
         */
        public MAGRequestBuilder setPublic() {
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
        public MAGRequestBuilder header(String name, String value) {
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
        public MAGRequestBuilder removeHeader(String name) {
            headers.remove(name);
            return this;
        }

        /**
         * <p>Register {@link HttpURLConnection} listener.</p>
         * The SDK uses Android {@link HttpURLConnection} to connect to the API; developers can
         * intercept the connection with {@link MAGConnectionListener}
         * before a request is sent to the backend service.
         *
         * @param listener The connection listener
         * @return The builder
         */
        public MAGRequestBuilder connectionListener(MAGConnectionListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Builds the {@link MAGRequest} object.
         *
         * @return An immutable {@link MAGRequest} object.
         */
        public MAGRequest build() {
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

            return new MAGRequest() {
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
                public GrantProvider getGrantProvider() {
                    return grantProvider;
                }

                @Override
                public MAGRequestBody getBody() {
                    return body;
                }

                @Override
                public MAGConnectionListener getConnectionListener() {
                    return listener;
                }

                @Override
                public MAGResponseBody getResponseBody() {
                    return responseBody;
                }

                @Override
                public String getScope() {
                    return scope;
                }

                @Override
                public boolean isPublic() {
                    return isPublic;
                }

            };
        }
    }
}
