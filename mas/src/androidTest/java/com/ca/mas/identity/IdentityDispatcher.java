package com.ca.mas.identity;

import com.ca.mas.TestUtils;
import com.ca.mas.core.http.ContentType;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.QueueDispatcher;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;

public class IdentityDispatcher extends QueueDispatcher {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static final String MODULE = "SCIM/MAS";
    public static final String VERSION = "v2";
    public static final String SCHEMAS = String.format("/%s/%s/Schemas/urn:ietf:params:scim:schemas:core:2.0:User", MODULE, VERSION);
    public static final String USERS = String.format("/%s/%s/Users/", MODULE, VERSION);
    public static final String USERS_FILTER = String.format("/%s/%s/Users?filter=", MODULE, VERSION);

    public static final String NOT_FOUND = "{\"detail\",\"Data with key %s not found\"}";

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

        try {
            if (request.getPath().equals(SCHEMAS) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            if (request.getPath().startsWith(USERS) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            if (request.getPath().startsWith(USERS_FILTER)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(String.format("/%s/%s/Users/filter", MODULE, VERSION)).toString());
            }

        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }


        return null;
    }
}
