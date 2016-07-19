/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.dynamicConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import com.ca.mas.core.MAGConstants;
import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.Server;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class DynamicConfigTest extends BaseTest {

    @Test
    public void returnNewInstanceWithDiffConfig() throws JSONException {
        MobileSso mobileSso1 = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        MobileSso mobileSso2 = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        assertEquals(mobileSso1, mobileSso2);
        MobileSso mobileSso3 = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));
        assertNotSame(mobileSso1, mobileSso3);
    }

    @Test
    public void restoreConnectedGatewayAfterShutdown() throws Exception {

        //Init with Gateway B
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));

        Server expected = ConfigurationManager.getInstance().getConnectedGateway();

        //Clear cache
        MobileSsoFactory.reset();

        //Init with Default interface
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Server actual = ConfigurationManager.getInstance().getConnectedGateway();

        assertEquals(expected, actual);

    }

    @Test
    public void storageContainsMultipleGatewayData() throws Exception {

        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config.json"));
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
        Server server1 = ConfigurationManager.getInstance().getConnectedGateway();

        //Perform Gateway Switch
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {
                
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
        Server server2 = ConfigurationManager.getInstance().getConnectedGateway();

        //Validate the result
        KeystoreDataSource<String, Object> keystoreDataSource = new KeystoreDataSource(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                null, null);

        List<String> keys = keystoreDataSource.getKeys(null);
        assertTrue(!keys.isEmpty());

        for (String k: keystoreDataSource.getKeys(null)) {
            if (!k.contains(server1.toString()) && !k.contains(server2.toString())) {
                fail();
            }
        }
    }

    @Test
    public void onlyDeregisterWithConnectedGateway() throws Exception {

        //Connect to Gateway1
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config.json"));
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);

        //Perform Gateway Switch to Gateway2
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);
        Server gateway2 = ConfigurationManager.getInstance().getConnectedGateway();

        mobileSso.removeDeviceRegistration();

        //Validate the result
        KeystoreDataSource<String, Object> keystoreDataSource = new KeystoreDataSource<String, Object>(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                null, null);

        List<String> keys = keystoreDataSource.getKeys(null);
        assertTrue(!keys.isEmpty());

        for (String k: keystoreDataSource.getKeys(null)) {
            //Failed if keychain contains gateway2 data.
            if (k.contains(gateway2.toString())) {
                fail();
            }
        }
    }

    @Test
    public void testDestroyAllTokens() throws Exception {
        storageContainsMultipleGatewayData();
        mobileSso.destroyAllPersistentTokens();
        KeystoreDataSource<String, Object> keystoreDataSource = new KeystoreDataSource<String, Object>(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                null, null);
        assertTrue(keystoreDataSource.getKeys(null).isEmpty());
    }

    @Test
    public void testForceDefault() throws Exception {
        ConfigurationManager.getInstance().setConfigurationFileName(getConfigJsonFileName());
        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);
        Server gateway1 = ConfigurationManager.getInstance().getConnectedGateway();

        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));
        Server gateway2 = ConfigurationManager.getInstance().getConnectedGateway();

        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);
        Server gateway3 = ConfigurationManager.getInstance().getConnectedGateway();

        assertNotSame(gateway1, gateway2);
        assertEquals(gateway1, gateway3);
    }

    @Test
    public void testSwitchReceiver() throws Exception {
        ConfigurationManager.getInstance().setConfigurationFileName(getConfigJsonFileName());

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        LocalBroadcastManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                countDownLatch.countDown();
            }
        }, new IntentFilter(MAGConstants.BEFORE_GATEWAY_SWITCH));

        LocalBroadcastManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                countDownLatch.countDown();
            }
        }, new IntentFilter(MAGConstants.AFTER_GATEWAY_SWITCH));


        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);

        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));

        countDownLatch.await();

    }

    @Override
    protected boolean initSDK() {
        return false;
    }

    @Override
    public void after() throws Exception {
        super.after();
        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).destroyAllPersistentTokens();
        MobileSsoFactory.reset();
    }
}
