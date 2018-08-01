/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Build;
import android.util.Log;

import com.ca.mas.AndroidVersionAwareTestRunner;
import com.ca.mas.MaxTargetAPI;
import com.ca.mas.MinTargetAPI;
import com.ca.mas.TestUtils;
import com.ca.mas.core.storage.implementation.MASStorageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * KeyStoreStorage Tests.
 * Uses the @{link BaseStorageTests} to do some of the basic tests.
 */
@RunWith(AndroidVersionAwareTestRunner.class)
@MaxTargetAPI(Build.VERSION_CODES.O_MR1)
public class KeyStoreStorageTests extends BaseStorageTests {

    private static final String TAG = KeyStoreStorageTests.class.getCanonicalName();


    private static String longKey = "PnkpzeEOOjTn1rkI8wOOBtcDdXNXiHb2KHJz37Be5QxAZRya3vSpjTuiA9AuTc7nflm39gKkbetOdg71";

    private static String longData;

    private static String longKeyAboveLimit;

    private static String longDataAboveLimit;


    @Override
    @Before
    public void setUp() throws Exception {

        longKeyAboveLimit = TestUtils.getString("/dumpKey_3382.txt");
        longData = TestUtils.getString("/dumpData_32768.txt");
        longDataAboveLimit = TestUtils.getString("/dumpData_32800.txt");


        try {
            currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, new Object[]{getContext(), false});
            assertNotNull(currentStorage);
        } catch (StorageException e) {
            if (e.getCode() == StorageException.STORE_NOT_UNLOCKED) {
                fail("KEYSTORE  is Locked! ");
            } else {
                fail("KEYSTORE Storage instantiation failed " + e);
            }

        }
    }


    /*
    Tests for validating the initialization of the Storage instance.
     */
    @Test
    public void testInitStorageWithNullInput() {
        Log.d(TAG, "testInitStorageWithNilInput");
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, null);
            fail("Failed to testInitStorageWithNilInput: Expected Exception ");
        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }
    }

    /*
    Test to verify if the KEYSTORE is unlocked.
    */
    @Test
    public void testKeyStoreUnlocked() {

        try {
            StorageResult result = currentStorage.readData("dummykey");
            if (result.getStatus() != StorageResult.StorageOperationStatus.SUCCESS) {
                StorageException error = (StorageException) result.getData();
                if (error.getCode() == StorageException.STORE_NOT_UNLOCKED) {
                    fail("KEYSTORE  is Locked! ");
                }
            }
        } catch (StorageException e) {
            if (e.getCode() == StorageException.STORE_NOT_UNLOCKED) {
                fail("KEYSTORE  is Locked! ");
            }
        }

    }

    @Test
    public void testInitStorageWithValidClassName() {
        Log.d(TAG, "testInitStorageWithValidClassName");
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage("com.ca.mas.core.storage.implementation.KeyStoreStorage", new Object[]{getContext(), true});
            assertNotNull(testStorage);
        } catch (StorageException e) {
            fail("Failed to testInitStorageWithValidClassName: reason  " + e);
        }
    }

    @Test
    public void testInitStorageWithInValidClassName() {
        Log.d(TAG, "testInitStorageWithNilInput");
        try {
            new MASStorageManager().getStorage("KeyStoreStorageTests", new Object[]{getContext(), true});
            fail("Failed to testInitStorageWithInValidClassName: Expected Exception ");
        } catch (StorageException e) {
            assertEquals(e.getCode(), StorageException.STORE_NOT_FOUND);
        }
    }


    //WRITE
    @Test
    public void testWriteLongKey() {
        Log.d(TAG, "testWriteLongKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData(longKey, bytes);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(longKey, result.getData().toString());
        } catch (StorageException e) {
            fail("Failed to testWriteLongKey");
        }
    }

    @Test
    public void testWriteLongKeyWithCallback() {
        Log.d(TAG, "testWriteLongKeyWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData(longKey, bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(longKey, result.getData().toString());
                }
            });

        } catch (StorageException e) {
            fail("Failed to testWriteLongKeyWithCallback");
        }
    }


    @Test
    public void testWriteLongKeyAboveMaxLimit() {
        Log.d(TAG, "testWriteLongKey");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData(longKeyAboveLimit, bytes);
            fail("Failed to testWriteLongKey");

        } catch (StorageException e) {
            assertEquals(e.getCode(), StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
    }

    @Test
    public void testWriteLongKeyAboveMaxLimitWithCallback() {
        Log.d(TAG, "testWriteLongKeyWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData(longKeyAboveLimit, bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("Failed to testWriteLongKeyWithCallback");

                }
            });

        } catch (StorageException e) {
            assertEquals(e.getCode(), StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
    }

    @Test
    public void testWriteLongData() {
        Log.d(TAG, "testWriteLongData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = longData;
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("Key1", bytes);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());

        } catch (StorageException e) {
            fail("Failed to testWriteLongData");
        }
    }

    @Test
    public void testWriteLongDataWithCallback() {
        Log.d(TAG, "testWriteLongDataWithCallback");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = longData;
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData("Key1", bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                }
            });

        } catch (StorageException e) {
            fail("Failed to testWriteLongDataWithCallback");
        }
    }

    @Test
    public void testWriteLongDataAboveMaxLimit() {
        Log.d(TAG, "testWriteLongDataAboveMaxLimit");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = longDataAboveLimit;
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData("Key1", bytes);
            fail("Failed to testWriteLongData");
        } catch (StorageException e) {
            assertEquals(e.getCode(), StorageException.KEYSTORE_DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    @Test
    public void testWriteLongDataAboveMaxLimitWithCallback() {
        Log.d(TAG, "testWriteLongDataAboveMaxLimitWithCallback");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = longDataAboveLimit;
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData("Key1", bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("Failed to testWriteLongDataWithCallback");
                }
            });

        } catch (StorageException e) {
            assertEquals(e.getCode(), StorageException.KEYSTORE_DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    @Test
    public void testWriteLongKeyAndData() {
        Log.d(TAG, "testWriteLongKeyAndData");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString(longKey, longData);
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(longKey, result.getData());

            result = currentStorage.readString(longKey);
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(longData, result.getData());


        } catch (StorageException e) {
            fail("Failed to testWriteLongKeyAndData");
        }
    }


    //READ
    @Test
    public void testReadLongKey() {
        Log.d(TAG, "testReadLongKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData(longKey, bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData(longKey);
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(str, new String((byte[]) result.getData()));
        } catch (StorageException e) {
            fail("Failed to testReadLongKey");
        }
    }

    @Test
    public void testReadLongKeyWithCallback() {
        Log.d(TAG, "testReadLongKeyWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        final String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData(longKey, bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.readData(longKey, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(str, new String((byte[]) result.getData()));
                }
            });

        } catch (StorageException e) {
            fail("Failed to testReadLongKeyWithCallback");
        }

    }


    //Combination test-cases

    @Override
    public void testWriteData() {
        super.testWriteData();
    }

    @Test
    public void testDataIndependenceBetweenSharedAndPrivateStore() {


        Log.d(TAG, "testWriteLongKeyAndData");

        StorageResult result;
        try {
            currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, new Object[]{getContext(), true});
            assertNotNull(currentStorage);
        } catch (StorageException e) {
            fail("Precondition fail: KEYSTORE with shared Storage,  instantiation failed " + e);
        }
        try {
            assertEquals(currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(currentStorage.writeString("Key1", "Value1").getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(currentStorage.writeString("Key2", "Value2").getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(((ArrayList) currentStorage.getAllKeys().getData()).size(), 2);
            try {
                currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, new Object[]{getContext(), false});
                assertNotNull(currentStorage);
            } catch (StorageException e) {
                fail("Precondition fail: KEYSTORE with shared Storage,  instantiation failed " + e);
            }
            assertEquals(currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(((ArrayList) currentStorage.getAllKeys().getData()).size(), 0);
            assertEquals(currentStorage.readString("Key1").getStatus(), StorageResult.StorageOperationStatus.FAILURE);
            assertEquals(currentStorage.writeString("Key1", "Value3").getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(currentStorage.readString("Key1").getData(), "Value3");
            try {
                currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, new Object[]{getContext(), true});
                assertNotNull(currentStorage);
            } catch (StorageException e) {
                fail("Precondition fail: KEYSTORE with shared Storage,  instantiation failed " + e);
            }
            assertEquals(((ArrayList) currentStorage.getAllKeys().getData()).size(), 2);
            assertEquals(currentStorage.readString("Key1").getData(), "Value1");
            assertEquals(currentStorage.readString("Key2").getData(), "Value2");
            assertEquals(currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(((ArrayList) currentStorage.getAllKeys().getData()).size(), 0);
            try {
                currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE, new Object[]{getContext(), false});
                assertNotNull(currentStorage);
            } catch (StorageException e) {
                fail("Precondition fail: KEYSTORE with shared Storage,  instantiation failed " + e);
            }
            assertEquals(((ArrayList) currentStorage.getAllKeys().getData()).size(), 1);


        } catch (StorageException e) {
            fail("Failed to testPrivateStore");
        }


    }
}
