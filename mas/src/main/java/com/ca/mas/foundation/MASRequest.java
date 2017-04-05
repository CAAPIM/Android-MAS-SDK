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
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.oauth.GrantProvider;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * An Http Api Request. Instances of this class are immutable.
 */
public interface MASRequest extends MAGRequest {

    /**
     * Notify the {@link MASCallback#onError(Throwable)} when the request is cancelled by {@link MAS#cancelRequest(long)}.
     * @return True to invoke {@link MASCallback#onError(Throwable)} with {@link com.ca.mas.foundation.MAS.RequestCancelledException} when the request is cancelled by
     * {@link MAS#cancelRequest(long)}. Default is false.
     */
    boolean notifyOnCancel();

    class MASRequestBuilder extends MAGRequestBuilder {

        private boolean notifyOnCancel = false;

        public MASRequestBuilder(URI uri) {
            super(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(uri.toString()));
        }

        public MASRequestBuilder(Uri uri) {
            super(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(uri.toString()));
        }

        public MASRequestBuilder(URL url) {
            super(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(url.toString()));
        }

        @Override
        public MASRequestBuilder get() {
            return (MASRequestBuilder) super.get();
        }

        public MASRequestBuilder post(MASRequestBody body) {
            return (MASRequestBuilder) super.post(body);
        }

        public MASRequestBuilder put(MASRequestBody body) {
            return (MASRequestBuilder) super.put(body);
        }

        public MASRequestBuilder delete(MASRequestBody body) {
            return (MASRequestBuilder) super.delete(body);
        }

        @Override
        public MASRequestBuilder password() {
            return (MASRequestBuilder) super.password();
        }

        @Override
        public MASRequestBuilder clientCredential() {
            return (MASRequestBuilder) super.clientCredential();
        }

        @Override
        public MASRequestBuilder scope(String scope) {
            return (MASRequestBuilder) super.scope(scope);
        }

        @Override
        public MASRequestBuilder responseBody(MAGResponseBody responseBody) {
            return (MASRequestBuilder) super.responseBody(responseBody);
        }

        @Override
        public MASRequestBuilder setPublic() {
            return (MASRequestBuilder) super.setPublic();
        }

        @Override
        public MASRequestBuilder header(String name, String value) {
            return (MASRequestBuilder) super.header(name, value);
        }

        @Override
        public MASRequestBuilder removeHeader(String name) {
            return (MASRequestBuilder) super.removeHeader(name);
        }

        @Override
        public MASRequestBuilder connectionListener(MAGConnectionListener listener) {
            return (MASRequestBuilder) super.connectionListener(listener);
        }

        public MASRequestBuilder notifyOnCancel() {
            this.notifyOnCancel = true;
            return this;
        }


        public MASRequest build() {
            final MAGRequest request = super.build();
            return new MASRequest() {

                @Override
                public boolean notifyOnCancel() {
                    return notifyOnCancel;
                }

                @Override
                public URL getURL() {
                    return request.getURL();
                }

                @Override
                public String getMethod() {
                    return request.getMethod();
                }

                @Override
                public Map<String, List<String>> getHeaders() {
                    return request.getHeaders();
                }

                @Override
                public GrantProvider getGrantProvider() {
                    return request.getGrantProvider();
                }

                @Override
                public MAGRequestBody getBody() {
                    return request.getBody();
                }

                @Override
                public MAGConnectionListener getConnectionListener() {
                    return request.getConnectionListener();
                }

                @Override
                public MAGResponseBody<?> getResponseBody() {
                    return request.getResponseBody();
                }

                @Override
                public String getScope() {
                    return request.getScope();
                }

                @Override
                public boolean isPublic() {
                    return request.isPublic();
                }
            };
        }
    }

}
