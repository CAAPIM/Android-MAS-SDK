package com.ca.mas.storage;

import com.ca.mas.core.http.ContentType;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.QueueDispatcher;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class StorageDispatcher extends QueueDispatcher {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static final String MODULE = "MASS";
    public static final String VERSION = "v1";
    public static final String CLIENT_DATA = String.format("/%s/%s/Client/Data", MODULE, VERSION);
    public static final String CLIENT_USER_DATA = String.format("/%s/%s/Client/User/Data", MODULE, VERSION);
    public static final String USER_DATA = String.format("/%s/%s/User/Data", MODULE, VERSION);

    public static final String NOT_FOUND = "{\"detail\",\"Data with key %s not found\"}";

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

        try {
            if (request.getPath().equals(CLIENT_DATA) ||
                    request.getPath().equals(CLIENT_USER_DATA) ||
                    request.getPath().equals(USER_DATA)
                    ) {
                switch (request.getMethod()) {
                    case GET:
                        return new MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_OK)
                                .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                                .setBody(MemoryStorage.getInstance().keySet(request.getPath()).toString());
                    default:
                        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

            }

            if (request.getPath().startsWith(CLIENT_DATA) ||
                    request.getPath().startsWith(CLIENT_USER_DATA) ||
                    request.getPath().startsWith(USER_DATA)
                    ) {
                switch (request.getMethod()) {
                    case GET:
                        if (MemoryStorage.getInstance().findByKey(request.getPath()) == null) {
                            return new MockResponse()
                                    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                                    .setBody(String.format(NOT_FOUND, request.getPath()));
                        }
                        return new MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_OK)
                                .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                                .setBody(MemoryStorage.getInstance().findByKey(request.getPath()).toString());

                    case PUT:
                        if (MemoryStorage.getInstance().findByKey(request.getPath()) == null) {
                            return new MockResponse()
                                    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                                    .setBody(String.format(NOT_FOUND, request.getPath()));
                        }
                        MemoryStorage.getInstance().save(request.getPath(), new JSONObject(request.getBody().readUtf8()));

                        return new MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);
                    case POST:
                        if (MemoryStorage.getInstance().findByKey(request.getPath()) != null) {
                            return new MockResponse()
                                    .setResponseCode(HttpURLConnection.HTTP_CONFLICT);
                        }
                        MemoryStorage.getInstance().save(request.getPath(), new JSONObject(request.getBody().readUtf8()));

                        return new MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);

                    case DELETE:
                        if (MemoryStorage.getInstance().findByKey(request.getPath()) == null) {
                            return new MockResponse()
                                    .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                                    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                                    .setBody(String.format(NOT_FOUND, request.getPath()));
                        }
                        MemoryStorage.getInstance().delete(request.getPath());
                        return new MockResponse()
                                .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);


                    default:
                        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }

            }
        } catch (JSONException e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        return null;
    }
}
