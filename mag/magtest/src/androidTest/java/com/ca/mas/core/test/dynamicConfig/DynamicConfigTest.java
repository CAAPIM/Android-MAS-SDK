/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.dynamicConfig;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.Server;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.OAuthTokenContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.test.BaseTest;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@Deprecated
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

    @Deprecated
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

    @Deprecated
    @Test
    public void storageContainsMultipleGatewayData() throws Exception {

        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config.json"));
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), null);
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

                mobileSso.authenticate(getUsername(), getPassword(), null);
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

        for (String k : keystoreDataSource.getKeys(null)) {
            if (!k.contains(server1.toString()) && !k.contains(server2.toString())) {
                fail();
            }
        }
    }


    @Deprecated
    @Test
    public void onlyDeregisterWithConnectedGateway() throws Exception {

        //Connect to Gateway1
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config.json"));
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), null);
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

                mobileSso.authenticate(getUsername(), getPassword(), null);
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

        for (String k : keystoreDataSource.getKeys(null)) {
            //Failed if keychain contains gateway2 data.
            if (k.contains(gateway2.toString())) {
                fail();
            }
        }
    }

    @Deprecated
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

    @Deprecated
    @Test
    public void testSwitchReceiver() throws Exception {
        ConfigurationManager.getInstance().setConfigurationFileName(getConfigJsonFileName());

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        EventDispatcher.BEFORE_GATEWAY_SWITCH.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                countDownLatch.countDown();
            }
        });

        EventDispatcher.AFTER_GATEWAY_SWITCH.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                countDownLatch.countDown();
            }
        });

        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);

        MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "msso_config_dynamic_test.json"));

        countDownLatch.await();

    }

    @Deprecated
    @Test
    public void testClientUpdate() throws Exception {
        //Connect to Gateway1
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config.json"));
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                mobileSso.authenticate(getUsername(), getPassword(), null);
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);

        //Client updated
        mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                getConfig(useMockServer(), "test_msso_config2.json"));

        ClientCredentialContainer cc = StorageProvider.getInstance().getClientCredentialContainer();
        assertNull(cc.getMasterClientId());
        assertNull(cc.getClientId());
        assertNull(cc.getClientSecret());

        OAuthTokenContainer oAuthTokenContainer = StorageProvider.getInstance().getOAuthTokenContainer();
        assertNull(oAuthTokenContainer.getAccessToken());
        assertNull(oAuthTokenContainer.getRefreshToken());
        assertNull(oAuthTokenContainer.getGrantedScope());

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
