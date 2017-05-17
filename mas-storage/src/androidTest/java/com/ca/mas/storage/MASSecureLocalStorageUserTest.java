/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.storage;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.foundation.MASConstants;

import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class MASSecureLocalStorageUserTest extends MASStorageTest {


    @Override
    int getMode() {
        return MASConstants.MAS_USER;
    }

    @Override
    MASStorage getMASStorage() {
        return new MASSecureLocalStorage();
    }

    @Test
    public void testDeleteAll() throws Exception {

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        MASSecureLocalStorage storage = (MASSecureLocalStorage) getMASStorage();
        storage.deleteAll(getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<Set<String>> keysetCallback = new MASCallbackFuture<>();
        getMASStorage().keySet(getMode(), keysetCallback);
        assertEquals(0, keysetCallback.get().size());
    }

}
