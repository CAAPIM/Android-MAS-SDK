/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.Server;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.OAuthTokenContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MASDynamicSDKTest extends MASStartTestBase {

    @Test
    public void restoreConnectedGatewayAfterShutdown() throws Exception {

        assertEquals("", MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

        MAS.start(getContext(), getConfig("/msso_config_dynamic_test.json"));

        assertEquals("test", MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

        //Reset the configuration from memory
        ConfigurationManager.getInstance().reset();

        //Init with Default interface
        MAS.start(getContext());

        assertEquals("test", MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

    }

    @Test
    public void storageContainsMultipleGatewayData() throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                MASUser.login("test", "test".toCharArray(), new MASCallback<MASUser>() {
                    @Override
                    public void onSuccess(MASUser result) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        Server server1 = new Server(MASConfiguration.getCurrentConfiguration().getGatewayHostName(),
                MASConfiguration.getCurrentConfiguration().getGatewayPort(),
                MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

        //Wait for request
        Thread.sleep(1000);

        //Perform Gateway Switch
        MAS.start(getContext(), getConfig("/msso_config_dynamic_test.json"));

        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        assertNotNull(callback2.get());

        Server server2 = new Server(MASConfiguration.getCurrentConfiguration().getGatewayHostName(),
                MASConfiguration.getCurrentConfiguration().getGatewayPort(),
                MASConfiguration.getCurrentConfiguration().getGatewayPrefix());


        //Validate the result
        DataSource clientCredentialStorage = getValue(StorageProvider.getInstance().getClientCredentialContainer(), "storage", DataSource.class );

        //It should only return the client credentials, however it returns everything in the key chain.
        List<String> keys = clientCredentialStorage.getKeys(null);
        assertEquals(16, keys.size());

        for (String k : keys) {
            if (!k.contains(server1.toString()) && !k.contains(server2.toString())) {
                fail();
            }
        }

        countDownLatch.await();
        DataSource shareStorage = getValue(StorageProvider.getInstance().getTokenManager(), "storage", DataSource.class );
        keys = shareStorage.getKeys(null);
        assertEquals(8, keys.size());

        for (String k : keys) {
            if (!k.contains(server1.toString()) && !k.contains(server2.toString())) {
                fail();
            }
        }

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Enumeration<String> enumeration = keyStore.aliases();

        List<String> storedKeys = Collections.list(enumeration);
        storedKeys.contains(server1.toString()+"msso.clientCertPrivateKey");
        storedKeys.contains(server2.toString()+"msso.clientCertPrivateKey");
        storedKeys.contains(server1.toString()+"msso.clientCertChain_1");
        storedKeys.contains(server2.toString()+"msso.clientCertChain_1");

    }


    @Test
    public void onlyDeregisterWithConnectedGateway() throws Exception {

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                MASUser.login("test", "test".toCharArray(), null);
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        Server server1 = new Server(MASConfiguration.getCurrentConfiguration().getGatewayHostName(),
                MASConfiguration.getCurrentConfiguration().getGatewayPort(),
                MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

        //Perform Gateway Switch

        MAS.start(getContext(), getConfig("/msso_config_dynamic_test.json"));

        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        assertNotNull(callback2.get());

        Server server2 = new Server(MASConfiguration.getCurrentConfiguration().getGatewayHostName(),
                MASConfiguration.getCurrentConfiguration().getGatewayPort(),
                MASConfiguration.getCurrentConfiguration().getGatewayPrefix());

        MASCallbackFuture<Void> deregisterCallback = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(deregisterCallback);
        deregisterCallback.get();

        //Validate the result
        KeystoreDataSource<String, Object> keystoreDataSource = new KeystoreDataSource(getContext(),
                null, null);

        List<String> keys = keystoreDataSource.getKeys(null);
        assertTrue(!keys.isEmpty());

        for (String k : keystoreDataSource.getKeys(null)) {
            //Failed if keychain contains gateway2 data.
            if (k.contains(server2.toString())) {
                fail();
            }
        }
    }

    @Test
    public void testDestroyAllTokens() throws Exception {
        storageContainsMultipleGatewayData();
        MASDevice.getCurrentDevice().resetLocally();

        KeystoreDataSource<String, Object> keystoreDataSource = new KeystoreDataSource<String, Object>(
                getContext(),
                null, null);
        assertTrue(keystoreDataSource.getKeys(null).isEmpty());
    }

    @Test
    public void testSwitchReceiver() throws Exception {

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

        MAS.start(getContext(), getConfig("/msso_config_dynamic_test.json"));

        countDownLatch.await();

    }

    @Test
    public void testClientUpdate() throws Exception {
        //Connect to Gateway1

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                MASUser.login("test", "test".toCharArray(), null);
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        //Client updated
        MAS.start(getContext(), getConfig("/msso_config2.json"));

        ClientCredentialContainer cc = StorageProvider.getInstance().getClientCredentialContainer();
        assertNull(cc.getMasterClientId());
        assertNull(cc.getClientId());
        assertNull(cc.getClientSecret());

        OAuthTokenContainer oAuthTokenContainer = StorageProvider.getInstance().getOAuthTokenContainer();
        assertNull(oAuthTokenContainer.getAccessToken());
        assertNull(oAuthTokenContainer.getRefreshToken());
        assertNull(oAuthTokenContainer.getGrantedScope());

    }


}
