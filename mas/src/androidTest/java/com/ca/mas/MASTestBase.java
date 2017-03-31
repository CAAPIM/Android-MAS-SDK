/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.io.IoUtils;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConnectionListener;
import com.squareup.okhttp.internal.SslContextBuilder;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public abstract class MASTestBase {

    protected static final int DEFAULT_MAX = 10485760;

    private static MockWebServer ssg;
    private HashMap<String, RecordedRequest> recordedRequests = new HashMap<>();
    private int requestTaken = 0;
    private GatewayDefaultDispatcher gatewayDefaultDispatcher = new GatewayDefaultDispatcher();

    @Before
    public void startServer() throws Exception {

        ssg = new MockWebServer();
        ssg.setDispatcher(gatewayDefaultDispatcher);
        ssg.useHttps(SslContextBuilder.localhost().getSocketFactory(), false);
        ssg.start(41979);

        //Turn on debug by default
        MAS.debug();

        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
                //If connect to localhost
                if (connection.getURL().getHost().equals("localhost")) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(SslContextBuilder.localhost().getSocketFactory());
                }
            }

            @Override
            public void onConnected(HttpURLConnection connection) {

            }
        });
    }

    @After
    public void shutDownServer() throws Exception {
        if (ssg != null) {
            ssg.shutdown();
        }
    }

    private void flushRequest() throws InterruptedException {
        int count = ssg.getRequestCount();
        count = count - requestTaken;
        for (int i = 0; i < count; i++) {
            RecordedRequest rr = ssg.takeRequest();
            Uri uri = Uri.parse(rr.getPath());
            recordedRequests.put(uri.getPath(), rr);
        }
        requestTaken = ssg.getRequestCount();
    }

    protected RecordedRequest getRecordRequest(String path) throws InterruptedException {
        flushRequest();
        return recordedRequests.get(path);
    }

    protected void setDispatcher(Dispatcher dispatcher) {
        ssg.setDispatcher(dispatcher);
    }

    protected int getPort() {
        return 41979;
    }

    protected String getHost() {
        return "localhost";
    }

    protected JSONObject getConfig(String path) throws Exception {
        byte[] bytes = IoUtils.slurpStream(getClass().getResourceAsStream(path), DEFAULT_MAX);
        JSONObject jsonObject = new JSONObject(new String(bytes));
        return jsonObject;
    }

    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    protected <T> T getValue(Object instance, String attribute, Class<T> returnType) {
        Field field = null;
        try {
            field = instance.getClass().getDeclaredField(attribute);
            field.setAccessible(true);
            return returnType.cast(field.get(instance));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


