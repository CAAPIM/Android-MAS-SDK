/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.storage;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;
import com.ca.mas.storage.MASSecureLocalStorage;
import com.ca.mas.storage.MASStorageSegment;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class MASSecureLocalStorageTest extends MASIntegrationBaseTest {


    private static final String TAG = "MASSecureLocalStorageTest";

    /**
     * The current storage instance
     */
    MASSecureLocalStorage localStorage;


    @Before
    public void setUp() throws Exception {
        try {
            localStorage = new MASSecureLocalStorage();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to initialize MASSecureLocalStorage " + e);
        }
    }

    @After
    public void tearDown() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.deleteAll(MASConstants.MAS_USER, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);

        final CountDownLatch latch2 = new CountDownLatch(1);
        localStorage.deleteAll(MASConstants.MAS_APPLICATION, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                latch2.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch2.countDown();
            }
        });
        await(latch2);


    }

    @Test(expected = NullPointerException.class)
    public void testSaveNull() {
        final String key = "key1";
        String value = null;

        localStorage.save(key, value, getMode(),null);
    }

    @Test(expected = NullPointerException.class)
    public void testSaveWithNullKey() {
        String key = null;
        String value = "value1";
        localStorage.save(key, value, getMode(), null);
    }


    @Test(expected = NullPointerException.class)
    public void testGetWithNullKey() {
        String key = null;
        localStorage.findByKey(key, getMode(), null);
    }

    @Test(expected = TypeNotPresentException.class)
    public void testSaveUnsupportedData() throws Exception {
        String key = "key1";
        Object unsupportedData = new Object();
        final CountDownLatch latch = new CountDownLatch(1);
        final Exception[] expected = new Exception[1];
        localStorage.save(key, unsupportedData, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                expected[0] = (Exception) e;
                latch.countDown();
            }
        });
        await(latch);
        if (expected[0] != null) {
            throw expected[0];
        }
        Assert.fail("Expected an Exception ");
    }

    //byte][ operation(s)

    @Test
    public void testGetByteArray() throws InterruptedException {
        final String key = "key1";
        byte[] value = "value1".getBytes();
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.findByKey(key, getMode(), new MASCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null && result instanceof byte[]) {
                            assertEquals(new String((byte[]) result), "value1");
                            assertResult[0] = true;
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }


    //String operations.

    @Test
    public void testSaveString() throws InterruptedException {
        final String key = "key1";
        String value = "value1";
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.findByKey(key, getMode(), new MASCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null && result instanceof String) {
                            assertEquals(result, "value1");
                            assertResult[0] = true;
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }

    @Test
    public void testSaveEmptyString() throws InterruptedException {
        final String key = "key1";
        String value = "";
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.findByKey(key, getMode(), new MASCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null && result instanceof String) {
                            assertEquals(result, "");
                            assertResult[0] = true;
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();

            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }


    @Test
    public void testUpdate() throws InterruptedException {
        final String key = "key1";
        final String value = "value1";
        final String value2 = "value2";
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.save(key, value2, getMode(), new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        localStorage.findByKey(key, getMode(), new MASCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                if (result.equals(value2)) {
                                    assertResult[0] = true;
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable e) {
                                latch.countDown();
                            }
                        });
                   }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();

            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }

    @Test
    public void testDeleteString() throws InterruptedException {

        final String key = "key1";
        String value = "";
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.delete(key, getMode(), new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        localStorage.findByKey(key, getMode(), new MASCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                if (result == null) {
                                    assertResult[0] = true;
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable e) {
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);

    }


    //JSON Operations
    @Test
    public void testSaveJson() throws JSONException, InterruptedException {
        final String key = "key1";
        final JSONObject value = new JSONObject("{'storage':'localstore'}");
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.findByKey(key, getMode(), new MASCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null && result instanceof JSONObject) {
                            assertEquals(result.toString(), value.toString());
                            assertResult[0] = true;
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }

    //Bitmap Operation
    @Test
    public void testGetBitmap() throws InterruptedException {
        final String key = "key1";
        Bitmap value = getBitmapFromAsset(InstrumentationRegistry.getInstrumentation().getTargetContext(), "samplepng.png");
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(1);
        localStorage.save(key, value, getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localStorage.findByKey(key, getMode(), new MASCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null && result instanceof Bitmap) {
                            assertResult[0] = true;
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }

    @Test
    public void testSameKeyForUserAndApplication() throws Exception {
        final String key = "key1";
        final JSONObject value = new JSONObject("{'storage':'localstore'}");
        final boolean[] assertResult = {false};
        final CountDownLatch latch = new CountDownLatch(2);
        localStorage.save(key, value, MASConstants.MAS_USER, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        localStorage.save(key, value, MASConstants.MAS_APPLICATION, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertResult[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        assertTrue(assertResult[0]);

    }

    @Test
    public void testDeleteAll() throws Exception {
        testSaveString();
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] assertResult = {false};
        localStorage.deleteAll(getMode(), new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        final CountDownLatch latch2 = new CountDownLatch(1);

        localStorage.keySet(getMode(), new MASCallback<Set<String>>() {
            @Override
            public void onSuccess(Set<String> result) {
                if (result.isEmpty()) {
                    assertResult[0] = true;
                }
                latch2.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch2.countDown();
            }
        });
        await(latch2);
        assertTrue(assertResult[0]);
    }

    // Utility methods.
    private static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr = null;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        } finally {
            //try {istr.close();} catch (IOException e) {e.printStackTrace();}
        }
        return bitmap;
    }

    protected
    @MASStorageSegment
    int getMode() {
        return MASConstants.MAS_USER;
    }
}
