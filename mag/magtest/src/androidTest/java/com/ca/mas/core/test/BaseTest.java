/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test;

import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.AuthenticationProvider;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {

    private static final String TAG = BaseTest.class.getCanonicalName();
    private static final String MSSO_CONFIG_JSON = "test_msso_config.json";

    @Rule
    public final ServiceTestRule rule = new ServiceTestRule();

    protected MockWebServer ssg;
    protected MobileSso mobileSso;
    protected MAGResponse response;
    protected MAGError error;
    protected long requestId;

    @After
    public void after() throws Exception {
        response = null;
        error = null;
        if (mobileSso != null) {
            try {
                mobileSso.removeDeviceRegistration();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        if (ssg != null) {
            ssg.shutdown();
        }

        MobileSsoFactory.reset();
    }

    @Before
    public void before() throws Exception {
        ConfigurationManager.getInstance().init(InstrumentationRegistry.getInstrumentation().getTargetContext());
        ssg = new MockWebServer();
        if (initSDK()) {
            mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), getConfig(useMockServer()));
            mobileSso.destroyAllPersistentTokens();
            mobileSso.setMobileSsoListener(new MobileSsoListener() {
                @Override
                public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {
                    mobileSso.authenticate(getUsername(), getPassword(), null);
                }

                @Override
                public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {
                }
            });
        }
        ssg.setDispatcher(new DefaultDispatcher());
        ssg.start(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenPort());
    }

    protected boolean initSDK() {
        return true;
    }

    protected String getUsername() {
        return "admin";
    }

    protected char[] getPassword() {
        return "7layer".toCharArray();
    }

    protected JSONObject getConfig(boolean useHttp, String configFileName) throws JSONException {
        InputStream is = null;
        StringBuilder jsonConfig = new StringBuilder();

        String fileName = getConfigJsonFileName();
        if (configFileName != null) {
            fileName = configFileName;
        }

        try {
            is = InstrumentationRegistry.getTargetContext().getAssets().open(fileName);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read Json Configuration file: " + fileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
        JSONObject result = new JSONObject(jsonConfig.toString());
        if (useHttp) {
            toAbsolutePath(result);
        }
        return result;
    }

    protected JSONObject getConfig(boolean useHttp) throws JSONException {
        return getConfig(useHttp, null);
    }

    private void toAbsolutePath(JSONObject o) throws JSONException {
        String host = o.getJSONObject("server").getString("hostname");
        int port = o.getJSONObject("server").getInt("port");
        String prefix = o.getJSONObject("server").optString("prefix", "");
        if (prefix != null && prefix.length() > 0) prefix = "/" + prefix;
        JSONObject endpoints = o.getJSONObject("oauth").getJSONObject("system_endpoints");
        String e = endpoints.getString("authorization_endpoint_path");
        endpoints.put("authorization_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("token_endpoint_path");
        endpoints.put("token_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("token_revocation_endpoint_path");
        endpoints.put("token_revocation_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("usersession_logout_endpoint_path");
        endpoints.put("usersession_logout_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("usersession_status_endpoint_path");
        endpoints.put("usersession_status_endpoint_path", "http://" + host + ":" + port + prefix + e);

        endpoints = o.getJSONObject("oauth").getJSONObject("oauth_protected_endpoints");
        e = endpoints.getString("userinfo_endpoint_path");
        endpoints.put("userinfo_endpoint_path", "http://" + host + ":" + port + prefix + e);

        endpoints = o.getJSONObject("mag").getJSONObject("system_endpoints");
        e = endpoints.getString("device_register_endpoint_path");
        endpoints.put("device_register_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("device_renew_endpoint_path");
        endpoints.put("device_renew_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("device_register_client_endpoint_path");
        endpoints.put("device_register_client_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("device_remove_endpoint_path");
        endpoints.put("device_remove_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("client_credential_init_endpoint_path");
        endpoints.put("client_credential_init_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("authenticate_otp_endpoint_path");
        endpoints.put("authenticate_otp_endpoint_path", "http://" + host + ":" + port + prefix + e);

        endpoints = o.getJSONObject("mag").getJSONObject("oauth_protected_endpoints");
        e = endpoints.getString("enterprise_browser_endpoint_path");
        endpoints.put("enterprise_browser_endpoint_path", "http://" + host + ":" + port + prefix + e);
        e = endpoints.getString("device_list_endpoint_path");
        endpoints.put("device_list_endpoint_path", "http://" + host + ":" + port + prefix + e);
    }

    protected String getConfigJsonFileName() {
        return MSSO_CONFIG_JSON;
    }

    protected URI getURI(String path) throws URISyntaxException {
        URI uri = mobileSso.getURI(path);
        if (useMockServer()) {
            return new URI(Uri.parse(uri.toString().replace("https", "http")).toString());
        }
        return uri;
    }

    protected String getIdToken() {
        return "dummy-idToken";
    }

    protected String getIdTokenType() {
        return "dummy-idTokenType";
    }


    public void setResponse(MAGResponse response) {
        this.response = response;
    }

    public void setError(MAGError error) {
        this.error = error;
    }

    protected boolean useMockServer() {
        return true;
    }

    protected long processRequest(MAGRequest request) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        long requestId = mobileSso.processRequest(request, new MAGResultReceiver() {
            @Override
            public void onSuccess(MAGResponse response) {
                setResponse(response);
                latch.countDown();
            }

            @Override
            public void onError(MAGError error) {
                setError(error);
                latch.countDown();
            }

            @Override
            public void onRequestCancelled(Bundle data) {
                latch.countDown();
            }

        });
        this.requestId=requestId;
        latch.await();
        return requestId;
    }

    protected void assumeMockServer() {
        Assume.assumeTrue(useMockServer());
    }

}


