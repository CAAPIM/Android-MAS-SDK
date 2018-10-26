/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.net.Uri;
import android.os.Build;

import com.ca.mas.AndroidVersionAwareTestRunner;
import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.MinTargetAPI;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidVersionAwareTestRunner.class)
public class MASSessionLockTest extends MASLoginTestBase {

    @Test
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testLockSession() throws Exception {
        MASUser currentUser = MASUser.getCurrentUser();
        String refreshToken = StorageProvider.getInstance().getOAuthTokenContainer().getRefreshToken();
        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        currentUser.lockSession(callbackFuture);
        try {
            callbackFuture.get();
        } catch (Exception e) {
            callbackFuture.onError(e);
        }

        assertTrue(currentUser.isSessionLocked());
        //When lockSession, the SDK invokes revoke in the background, we have to sleep for a while and wait
        //for the revoke to complete
        Thread.sleep(500);
        RecordedRequest recordedRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_REVOKE);
        Uri uri = Uri.parse(recordedRequest.getPath());
        //Make sure we are using refresh to token to revoke.
        Assert.assertEquals(uri.getQueryParameter("token"), refreshToken);

        currentUser.removeSessionLock(null);
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testLockSessionAlreadyLocked() throws Exception {
        MASUser currentUser = MASUser.getCurrentUser();
        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        currentUser.lockSession(callbackFuture);
        try {
            callbackFuture.get();
        } catch (Exception e) {
            callbackFuture.onError(e);
        }

        assertTrue(currentUser.isSessionLocked());
        currentUser.lockSession(callbackFuture);
        assertTrue(currentUser.isSessionLocked());

        currentUser.removeSessionLock(null);
    }

    @Test(expected = SecureLockException.class)
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testLockSessionMissingIdToken() throws Throwable {
        MASUser currentUser = MASUser.getCurrentUser();
        TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();
        try {
            tokenManager.deleteIdToken();
        } catch (TokenStoreException e) {
        }

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        currentUser.lockSession(callbackFuture);
        try {
            callbackFuture.get();
        } catch (Exception e) {
            throw ((MASException) e.getCause()).getRootCause();
        }
    }

    @Test(expected = Exception.class)
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testUnlockSessionCallsOnUserAuthenticationRequired() throws Throwable {
        MASUser currentUser = MASUser.getCurrentUser();
        MASSessionUnlockCallbackFuture<Void> callbackFuture = new MASSessionUnlockCallbackFuture<>();
        MASCallbackFuture<Void> masCallbackFuture = new MASCallbackFuture<>();
        currentUser.lockSession(masCallbackFuture);

        try {
            masCallbackFuture.get();
            currentUser.unlockSession(callbackFuture);
            try {
                callbackFuture.get();
            } catch (Exception e) {
                assertEquals(e.getMessage(), "java.lang.Exception: User authentication required.");
                throw e;
            }
        } catch (Exception e) {
            throw e.getCause();
        } finally {
            currentUser.removeSessionLock(null);
        }
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testUnlockSessionMissingIdToken() throws Throwable {
        MASUser currentUser = MASUser.getCurrentUser();
        MASSessionUnlockCallbackFuture<Void> callbackFuture = new MASSessionUnlockCallbackFuture<>();
        currentUser.lockSession(callbackFuture);
        currentUser.removeSessionLock(null);
        currentUser.unlockSession(callbackFuture);

        assertFalse(currentUser.isSessionLocked());
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.M)
    public void testRemoveSessionLockAlreadyUnlocked() throws Throwable {
        MASUser currentUser = MASUser.getCurrentUser();
        MASSessionUnlockCallbackFuture<Void> callbackFuture = new MASSessionUnlockCallbackFuture<>();
        currentUser.lockSession(callbackFuture);
        currentUser.removeSessionLock(null);
        currentUser.removeSessionLock(callbackFuture);

        assertNull(callbackFuture.get());
        assertFalse(currentUser.isSessionLocked());
    }
}
