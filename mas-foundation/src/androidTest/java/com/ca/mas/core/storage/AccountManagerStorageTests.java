/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Build;

import com.ca.mas.AndroidVersionAwareTestRunner;
import com.ca.mas.MinTargetAPI;
import com.ca.mas.TestUtils;
import com.ca.mas.core.storage.implementation.MASStorageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@RunWith(AndroidVersionAwareTestRunner.class)
@MinTargetAPI(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AccountManagerStorageTests extends BaseStorageTestsClone {

    private static String reallyLongData;


    @Override
    @Before
    public void setUp() throws Exception {

        reallyLongData = TestUtils.getString("/dumpData_32800.txt");

        try {
            currentStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_AMS, new Object[]{getContext(), false});
            assertNotNull(currentStorage);
        } catch (StorageException e) {
            fail("Failed to initialize storage of type TYPE_AMS " + e);
        }
    }


    @Override
    @After
    public void tearDown() throws Exception {
        if (isSkipped) return;
        currentStorage.deleteAll();
    }


    @Test
    public void testInitStorageWithNullInput() {
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_AMS, new Object[]{null, null});
            fail("Failed to testInitStorageWithNilInput: Expected Exception ");
        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }
    }

    @Test
    public void testInitStorageWithInValidInputAndNoOptionalParameter() {
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_AMS, new Object[]{null});
            fail("Failed to testInitStorageWithInValidInputAndNoOptionalParameter:Expected Exception");
        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }
    }

    @Test
    public void testInitStorageWithValidClassNameAndOneParameter() {
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage("com.ca.mas.core.storage.implementation.AccountManagerStorage", new Object[]{getContext()});
            assertNotNull(testStorage);
        } catch (StorageException e) {
            fail("Failed to testInitStorageWithValidClassNameAndOneParameter: reason  " + e);
        }
    }

    @Test
    public void testInitStorageWithValidClassNameAndInvalidSecondParameter() {
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage("com.ca.mas.core.storage.implementation.AccountManagerStorage", new Object[]{getContext(), null});
            assertNotNull(testStorage);
        } catch (StorageException e) {
            fail("Failed to testInitStorageWithValidClassNameAndInvalidSecondParameter");
        }
    }

    @Test
    public void testInitStorageWithValidClassName() {
        Storage testStorage = null;
        try {
            testStorage = new MASStorageManager().getStorage("com.ca.mas.core.storage.implementation.AccountManagerStorage", new Object[]{getContext(), false});
            assertNotNull(testStorage);
        } catch (StorageException e) {
            fail("Failed to testInitStorageWithValidClassName: reason  " + e);
        }
    }

    @Test
    public void testGetStorageType() {
        try {
            assertEquals(currentStorage.getType(), MASStorageManager.MASStorageType.TYPE_AMS);
        } catch (Exception e) {
            fail("failed the getType()");
        }
    }

    @Test
    public void testWriteData() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");


        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }
    }


    @Test
    public void testWriteLongKeyWithCallback() {
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            final String longData = reallyLongData + reallyLongData;// 65600 bytes
            currentStorage.writeData(longData, bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(longData, result.getData().toString());
                }
            });

        } catch (StorageException e) {
            fail("Failed to testWriteLongKeyWithCallback");
        }
    }


    @Test
    public void testWriteLongData() {
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = reallyLongData + reallyLongData;//65600 bytes
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
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = reallyLongData + reallyLongData;//65600 bytes
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
    public void testWriteLongKeyAndData() {

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            String longData = reallyLongData + reallyLongData;//65600 bytes
            String longKey = reallyLongData + reallyLongData;//65600 bytes
            result = currentStorage.writeString(longKey, longData);
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(longData, result.getData());

            result = currentStorage.readString(longKey);
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(longData, result.getData());


        } catch (StorageException e) {
            fail("Failed to testWriteLongKeyAndData");
        }
    }

    @Test
    public void testReadData() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");

        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }

    }

    @Test
    public void testReadString() {
        StorageResult result;
        try {
            result = currentStorage.writeString("key23", "value23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getData(), "key23");
            result = currentStorage.readString("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getData(), "value23");

        } catch (StorageException e) {
            assertNotNull(e);
            assertEquals(e.getCode(), StorageException.INVALID_INPUT);
        }

    }

    @Test
    public void testUpdateData() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");
            result = currentStorage.updateData("key23", "value26".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value26");


        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateString() {
        StorageResult result;
        try {
            result = currentStorage.writeString("key23", "value23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readString("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getData(), "value23");
            result = currentStorage.updateString("key23", "value26");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readString("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getData(), "value26");


        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteOrUpdateData() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");
            result = currentStorage.writeOrUpdateData("key23", "value26".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getType(), StorageResult.StorageOperationType.WRITE_OR_UPDATE);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value26");

        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteOrUpdateString() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");
            result = currentStorage.writeOrUpdateString("key23", "value26");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getType(), StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value26");

        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllKeys() {
        StorageResult result;
        try {
            List<String> keys = new ArrayList<String>();
            keys.add("key1");
            keys.add("key2");
            keys.add("key3");


            result = currentStorage.writeData("key1", "value1".getBytes("UTF-8"));
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key1");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value1");

            result = currentStorage.writeData("key2", "value2".getBytes("UTF-8"));
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key2");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value2");

            result = currentStorage.writeData("key3", "value3".getBytes("UTF-8"));
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key3");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value3");


            result = currentStorage.getAllKeys();
            assertNotNull(result);
            assertEquals(result.getType(), StorageResult.StorageOperationType.GET_ALL_KEYS);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(result.getData() instanceof ArrayList, true);
            ArrayList<String> resultKeys = (ArrayList<String>) result.getData();
            assertEquals(resultKeys.size(), 3);
        } catch (StorageException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testDeleteData() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            assertEquals(new String((byte[]) result.getData()), "value23");
            result = currentStorage.deleteData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.FAILURE);

        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteAll() {
        StorageResult result;
        try {
            result = currentStorage.writeData("key23", "value23".getBytes());
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.deleteAll();
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("key23");
            assertNotNull(result);
            assertEquals(result.getStatus(), StorageResult.StorageOperationStatus.FAILURE);

        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadTest() {
        String value = "Test value";
        String keyName = "key";

        Storage d = currentStorage;

        long startTime = System.nanoTime();
        for(int i = 0; i<100 ; i++ ){
            keyName = keyName + i;

            try {
                d.writeData(keyName,value.getBytes());
            } catch (StorageException e) {
                e.printStackTrace();
            }
            keyName = "key";
        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;

        // - nano to miliseconds
        totalTime = totalTime/1000000;

        // - more then 3.5 seconds force fail
        if (totalTime > 3500) {
            fail();
        }
    }


}
