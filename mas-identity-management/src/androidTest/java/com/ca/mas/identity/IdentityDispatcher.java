package com.ca.mas.identity;

import com.ca.mas.TestUtils;
import com.ca.mas.core.http.ContentType;
import java.net.HttpURLConnection;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;

public class IdentityDispatcher extends QueueDispatcher {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static final String MODULE = "SCIM/MAS";
    public static final String VERSION = "v2";
    public static final String SCHEMAS_USER = String.format("/%s/%s/Schemas/urn:ietf:params:scim:schemas:core:2.0:User", MODULE, VERSION);
    public static final String SCHEMAS_GROUP = String.format("/%s/%s/Schemas/urn:ietf:params:scim:schemas:core:2.0:Group", MODULE, VERSION);
    public static final String USERS = String.format("/%s/%s/Users/", MODULE, VERSION);
    public static final String GROUPS = String.format("/%s/%s/Groups", MODULE, VERSION);
    public static final String USERS_FILTER = String.format("/%s/%s/Users?", MODULE, VERSION);
    public static final String GROUPS_FILTER = String.format("/%s/%s/Groups?", MODULE, VERSION);

    public static final String NOT_FOUND = "{\"detail\",\"Data with key %s not found\"}";

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

        try {
            //User
            if (request.getPath().equals(SCHEMAS_USER) && request.getMethod().equals(GET)) {
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

            if (request.getPath().startsWith(USERS) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            //Group schema
            if (request.getPath().equals(SCHEMAS_GROUP) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            //Create Group
            if (request.getPath().equals(GROUPS) && request.getMethod().equals(POST)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            //Group Filtering
            if (request.getPath().startsWith(GROUPS_FILTER) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(String.format("/%s/%s/Groups/filter", MODULE, VERSION)).toString());
            }

            //Get Group By ID
            if (request.getPath().startsWith(GROUPS) && request.getMethod().equals(GET)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(TestUtils.getJSONObject(request.getPath()).toString());
            }

            if (request.getPath().startsWith(GROUPS) && request.getMethod().equals(DELETE)) {
                return new MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);
            }

        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }


        return null;
    }
}
