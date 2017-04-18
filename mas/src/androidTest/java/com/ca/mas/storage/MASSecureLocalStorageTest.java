/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.foundation.MASConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MASSecureLocalStorageTest extends MASLoginTestBase {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws Exception {
        MASCallbackFuture<Void> deleteAllUserDataCallback = new MASCallbackFuture<Void>();
        getStorage().deleteAll(MASConstants.MAS_USER, deleteAllUserDataCallback);
        deleteAllUserDataCallback.get();

        MASCallbackFuture<Void> deleteAllApplicationDataCallback = new MASCallbackFuture<Void>();
        getStorage().deleteAll(MASConstants.MAS_APPLICATION, deleteAllApplicationDataCallback);
        deleteAllApplicationDataCallback.get();

    }

    @Test(expected = NullPointerException.class)
    public void testSaveNull() {
        final String key = "key1";
        String value = null;

        getStorage().save(key, value, getMode(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testSaveWithNullKey() {
        String key = null;
        String value = "value1";
        getStorage().save(key, value, getMode(), null);
    }


    @Test(expected = NullPointerException.class)
    public void testGetWithNullKey() {
        String key = null;
        getStorage().findByKey(key, getMode(), null);
    }

    @Test(expected = TypeNotPresentException.class)
    public void testSaveUnsupportedData() throws Throwable {
        String key = "key1";
        Object unsupportedData = new Object();
        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<Void>();

        getStorage().save(key, unsupportedData, getMode(), callbackFuture);
        try {
            callbackFuture.get();
            fail();
        } catch (ExecutionException e) {
            throw e.getCause().getCause();
        }
    }

    //byte][ operation(s)

    @Test
    public void testGetByteArray() throws ExecutionException, InterruptedException {
        final String key = "key1";
        byte[] value = "value1".getBytes();
        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();

        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();
        MASCallbackFuture<byte[]> findCallback = new MASCallbackFuture<byte[]>();

        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertEquals(new String(value), new String(findCallback.get()));
    }


    //String operations.
    @Test
    public void testSaveString() throws InterruptedException, ExecutionException {
        final String key = "key1";
        String value = "value1";
        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();

        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();
        MASCallbackFuture<String> findCallback = new MASCallbackFuture<String>();

        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertEquals(value, findCallback.get());
    }

    @Test
    public void testSaveEmptyString() throws InterruptedException, ExecutionException {
        final String key = "key1";
        String value = "";

        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();

        MASCallbackFuture<String> findCallback = new MASCallbackFuture<String>();
        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertEquals(value, findCallback.get());
    }


    @Test
    public void testUpdate() throws InterruptedException, ExecutionException {
        final String key = "key1";
        final String value = "value1";
        final String value2 = "value2";


        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();

        MASCallbackFuture<Void> saveCallback2 = new MASCallbackFuture<Void>();
        getStorage().save(key, value2, getMode(), saveCallback2);
        saveCallback2.get();

        MASCallbackFuture<String> findCallback = new MASCallbackFuture<String>();
        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertEquals(value2, findCallback.get());

    }

    @Test
    public void testDeleteString() throws InterruptedException, ExecutionException {

        final String key = "key1";
        String value = "";

        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();

        MASCallbackFuture<Void> deleteCallback = new MASCallbackFuture<Void>();
        getStorage().delete(key, getMode(), deleteCallback);
        deleteCallback.get();

        MASCallbackFuture findCallback = new MASCallbackFuture();
        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertNull(findCallback.get());

    }


    //JSON Operations
    @Test
    public void testSaveJson() throws JSONException, InterruptedException, ExecutionException {
        final String key = "key1";
        final JSONObject value = new JSONObject("{'storage':'localstore'}");

        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();

        MASCallbackFuture<JSONObject> findCallback = new MASCallbackFuture<JSONObject>();
        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertEquals(value.toString(), findCallback.get().toString());

    }

    //Bitmap Operation
    @Test
    public void testGetBitmap() throws InterruptedException, IOException, ExecutionException {
        final String key = "key1";
        Bitmap value = getImage("/samplepng.png");

        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, getMode(), saveCallback);
        saveCallback.get();

        MASCallbackFuture<Bitmap> findCallback = new MASCallbackFuture<Bitmap>();
        getStorage().findByKey(key, getMode(), findCallback);
        Assert.assertNotNull(findCallback.get());

    }

    @Test
    public void testSameKeyForUserAndApplication() throws Exception {
        final String key = "key1";
        final String value = "value1";
        final String value2 = "value2";

        MASCallbackFuture<Void> saveCallback = new MASCallbackFuture<Void>();
        getStorage().save(key, value, MASConstants.MAS_USER, saveCallback);
        saveCallback.get();

        MASCallbackFuture<Void> saveCallback2 = new MASCallbackFuture<Void>();
        getStorage().save(key, value2, MASConstants.MAS_APPLICATION, saveCallback2);
        saveCallback2.get();

        MASCallbackFuture<String> findCallback = new MASCallbackFuture<String>();
        getStorage().findByKey(key, MASConstants.MAS_USER, findCallback);
        Assert.assertEquals(value, findCallback.get());

        MASCallbackFuture<String> findCallback2 = new MASCallbackFuture<String>();
        getStorage().findByKey(key, MASConstants.MAS_APPLICATION, findCallback2);
        Assert.assertEquals(value2, findCallback2.get());

    }

    @Test
    public void testDeleteAll() throws Exception {
        testSaveString();
        MASCallbackFuture<Void> deleteCallback = new MASCallbackFuture<Void>();
        getStorage().deleteAll(getMode(), deleteCallback);
        MASCallbackFuture<Set<String>> keysetCallback = new MASCallbackFuture<Set<String>>();
        getStorage().keySet(getMode(), keysetCallback);
        assertTrue(keysetCallback.get().isEmpty());
    }

    // Utility methods.
    private Bitmap getImage(String filePath) throws IOException {

        byte[] bytes = IoUtils.slurpStream(getClass().getResourceAsStream(filePath), DEFAULT_MAX_RESPONSE_SIZE);
        InputStream istr = null;
        Bitmap bitmap = null;
        istr = new ByteArrayInputStream(bytes);
        bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    protected
    @MASStorageSegment
    int getMode() {
        return MASConstants.MAS_USER;
    }

    protected MASSecureLocalStorage getStorage() {
        return new MASSecureLocalStorage();
    }
}
