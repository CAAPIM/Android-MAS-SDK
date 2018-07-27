/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Build;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.MASTestBase;
import com.ca.mas.MaxTargetAPI;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Storage implementations can leverage some basis tests, that are already written by extending
 * this class instead of the  ApplicationTestCase and by overriding the setup() to initialize
 * {@code currentStorage} with a valid storage instance.
 * <p/>
 *
 * This is a clone from {@link BaseStorageTests}, limitation to only set @MaxTargetAPI(Build.VERSION_CODES.O) on
 * {@link KeyStoreStorageTests}
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseStorageTestsClone extends MASTestBase {


    private static final String TAG = BaseStorageTestsClone.class.getCanonicalName();

    /**
     * The current storage instance
     */
    protected Storage currentStorage;


    /**
     * Implementations should override and assign proper value currentStorage to run the base tests
     *
     * @throws Exception
     */
    abstract public void setUp() throws Exception;

    @After
    public void tearDown() throws Exception {
        if (isSkipped) return;
        try {
            currentStorage.deleteAll();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /*
        Test to write the Data to the Storage
         */
    @Test
    public void testWriteData() {
        Log.d(TAG, "testWriteData");
        StorageResult result = null;
        try {
            assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.writeData("Key1", "TestString".getBytes("UTF-8"));
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData().toString());
        } catch (Exception e) {
            fail("failed to testWriteData " + e);
        }
    }

    @Test
    public void testWriteDataWithCallback() {
        Log.d(TAG, "testWriteDataWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeData("Key1", "TestString".getBytes(), new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key1", result.getData().toString());
                }
            });
        } catch (StorageException e) {
            fail("Failed to writeDataWithCallBack");
        }
    }

    @Test
    public void testWriteEmptyData() {
        Log.d(TAG, "testWriteEmptyData");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "";
        byte[] bytes = str.getBytes();
        try {
            StorageResult result = currentStorage.writeData("Key1", bytes);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData().toString());
        } catch (StorageException e) {
            fail("Failed to WriteEmptyData");
        }
    }

    @Test
    public void testWriteEmptyDataWithCallback() {
        Log.d(TAG, "testWriteEmptyDataWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData("Key1", bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key1", result.getData().toString());
                }
            });
        } catch (StorageException e) {
            fail("Failed to testWriteEmptyDataWithCallback");
        }
    }

    @Test
    public void testWriteDataWithEmptyKey() {
        Log.d(TAG, "testWriteDataWithEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("", bytes);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("", result.getData().toString());
        } catch (StorageException e) {
            fail("Failed to testWriteDataWithEmptyKey");
        }
    }

    @Test
    public void testWriteDataWithEmptyKeyCallback() {
        Log.d(TAG, "testWriteDataWithEmptyKeyCallback");

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData("", bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("", result.getData().toString());
                }
            });
        } catch (StorageException e) {
            fail("Failed to testWriteDataWithEmptyKeyCallback");
        }
    }

    @Test
    public void testWriteDataAlreadyExists() {
        Log.d(TAG, "testWriteDataAlreadyExists");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        String updateStr = "TestString2";
        byte[] updateBytes = updateStr.getBytes();
        try {
            assertEquals("precondition: initial write failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.writeData("Key1", updateBytes);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            assertTrue("Result data is not of the type StorageException", result.getData() instanceof StorageException);
            assertEquals(((StorageException) result.getData()).getCode(), StorageException.WRITE_DATA_ALREADY_EXISTS);
        } catch (StorageException e) {
            fail("Failed to testWriteDataAlreadyExists");
        }
    }

    @Test
    public void testWriteDataAlreadyExistsWithCallback() {
        Log.d(TAG, "testWriteDataAlreadyExistsWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        String updateStr = "TestString2";
        byte[] updateBytes = updateStr.getBytes();
        try {
            assertEquals("precondition: initial write failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.writeData("Key1", updateBytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                    assertTrue("Result data is not of the type StorageException", result.getData() instanceof StorageException);
                    assertEquals(((StorageException) result.getData()).getCode(), StorageException.WRITE_DATA_ALREADY_EXISTS);
                }
            });
        } catch (StorageException e) {
            fail("Failed to testWriteDataAlreadyExistsWithCallback");
        }
    }


    @Test
    public void testWriteNilData() {
        Log.d(TAG, "testWriteNilData");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeData("Key1", null);
            fail("Failed to testWriteNilData");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteNilDataWithCallback() {
        Log.d(TAG, "testWriteNilDataWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeData("Key1", null, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("Failed to testWriteNilDataWithCallback- Expected Execution to be thrown");
                }
            });
            fail("Failed to testWriteNilDataWithCallback");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteNilKey() {
        Log.d(TAG, "testWriteNilKey");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData(null, bytes);
            fail("Failed to testWriteNilKey");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testWriteNilKeyWithCallback() {
        Log.d(TAG, "testWriteNilKey");
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            currentStorage.writeData(null, bytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("Failed to testWriteNilKeyWithCallback- Expected Exception");
                }
            });
            fail("Failed to testWriteNilKeyWithCallback");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }



    /*
    Tests to WRITE String objects to the Storage.
     */

    @Test
    public void testWriteStringSuccess() {
        Log.d(TAG, "testWriteStringSuccess");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "TestString");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key", result.getData());

        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    @Test
    public void testWriteStringSuccessWithCallBack() {
        Log.d(TAG, "testWriteStringSuccessWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("Key", "TestString", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key", result.getData());
                }
            });
        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }


    @Test
    public void testWriteStringWithUnicode() {
        Log.d(TAG, "testWriteStringWithUnicode");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "abcd 測試");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key", result.getData());

        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    @Test
    public void testWriteStringWithUnicodeWithCallBack() {
        Log.d(TAG, "testWriteStringWithUnicodeWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("Key", "abcd 測試", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key", result.getData());
                }
            });
        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    @Test
    public void testWriteStringAlreadyExistFailure() {
        Log.d(TAG, "testWriteStringAlreadyExistFailure");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeString("Key", "TestString2");
                assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                assertTrue("Result is not of the type StorageException", result.getData() instanceof StorageException);
                assertEquals(StorageException.WRITE_DATA_ALREADY_EXISTS, ((StorageException) result.getData()).getCode());

            } else {
                fail("Precondition WRITE failed");
            }
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteStringAlreadyExistFailureWithCallBack() {
        Log.d(TAG, "testWriteStringAlreadyExistFailureWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeString("Key", "TestString2", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                        assertTrue("Result is not of the type StorageException", result.getData() instanceof StorageException);
                        assertEquals(StorageException.WRITE_DATA_ALREADY_EXISTS, ((StorageException) result.getData()).getCode());
                    }
                });
            } else {
                fail("Precondition WRITE failed");
            }
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteStringNilKeyFailure() {
        Log.d(TAG, "testWriteStringNilKeyFailure");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString(null, "TestString");
            fail("testWriteStringNilKeyFailure should throw exception");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testWriteStringNilKeyFailureWithCallBack() {
        Log.d(TAG, "testWriteStringNilKeyFailureWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString(null, "TestString", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("testWriteStringNilKeyFailure should throw exception");
                }
            });
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }


    @Test
    public void testWriteStringNilData() {
        Log.d(TAG, "testWriteStringNilData");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("Key", null);
            fail("testWriteStringNilKeyFailure should throw exception");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteStringNilDataWithCallBack() {
        Log.d(TAG, "testWriteStringNilDataWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("Key", null, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("testWriteStringNilKeyFailure should throw exception");
                }
            });
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteStringEmptyData() {
        Log.d(TAG, "testWriteStringEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key", result.getData());

        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    @Test
    public void testWriteStringEmptyDataWithCallBack() {
        Log.d(TAG, "testWriteStringEmptyDataWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("Key", "", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key", result.getData());
                }
            });
        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }


    @Test
    public void testWriteStringEmptyKey() {
        Log.d(TAG, "testWriteStringEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("", "TestString");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("", result.getData());

        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    @Test
    public void testWriteStringEmptyKeyWithCallBack() {
        Log.d(TAG, "testWriteStringEmptyKeyWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.writeString("", "TestString", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("", result.getData());
                }
            });
        } catch (StorageException e) {
            fail("failed to testWriteStringSuccess");
        }
    }

    /*
    Tests to READ the string from the Storage.
     */


    @Test
    public void testReadData() {
        Log.d(TAG, "testReadData");

        StorageResult result = null;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.deleteAll();
            String str = "TestString";
            byte[] bytes = str.getBytes();
            assertEquals("Precondition WRITE failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("Key1");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(str, new String((byte[]) result.getData()));


        } catch (Exception e) {
            fail("Failed to Read Data");
        }
    }

    @Test
    public void testReadDataWithCallback() {
        Log.d(TAG, "testReadDataWithCallback");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        final String str = "TestString";
        byte[] bytes = str.getBytes();

        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.readData("Key1", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(str, new String((byte[]) result.getData()));
                }
            });
        } catch (StorageException e) {
            fail("Failed to testReadDataWithCallback");
        }
    }

    @Test
    public void testReadEmptyData() {
        Log.d(TAG, "testReadEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("Key1");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("", new String((byte[]) result.getData()));
        } catch (StorageException e) {
            fail("Failed to testReadEmptyData");
        }
    }

    @Test
    public void testReadEmptyDataWithCallback() {
        Log.d(TAG, "testReadEmptyDataWithCallback");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.readData("Key1", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("", new String((byte[]) result.getData()));
                }
            });
        } catch (StorageException e) {
            fail("Failed to testReadEmptyDataWithCallback");
        }
    }

    @Test
    public void testReadDataWithEmptyKey() {
        Log.d(TAG, "testReadDataWithEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(str, new String((byte[]) result.getData()));


        } catch (StorageException e) {
            fail("Failed to testReadDataWithEmptyKey");
        }
    }

    @Test
    public void testReadDataWithEmptyKeyCallback() {
        Log.d(TAG, "testReadDataWithEmptyKeyCallback");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        final String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.readData("", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(str, new String((byte[]) result.getData()));
                }
            });
        } catch (StorageException e) {
            fail("Failed to testReadDataWithEmptyKeyCallback");
        }
    }


    @Test
    public void testReadDataWithNilKey() {
        Log.d(TAG, "testReadDataWithNilKey");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.readData(null);
            fail("Failed to testReadDataWithNilKey");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testReadDataWithNilKeyCallBack() {
        Log.d(TAG, "testReadDataWithNilKeyCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.readData(null, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("Failed to testReadDataWithNilKeyCallBack: Expected Execution");
                }
            });
            fail("Failed to testReadDataWithNilKeyCallBack");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testReadPreserveKey() {
        Log.d(TAG, "testReadPreserveKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("  Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.readData("  Key1");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(str, new String((byte[]) result.getData()));

        } catch (StorageException e) {
            fail("Failed to testReadPreserveKey");
        }
    }

    @Test
    public void testReadPreserveKeyWithCallBack() {
        Log.d(TAG, "testReadPreserveKeyWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        final String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            assertEquals("Precondition WRITE failed", currentStorage.writeData("  Key1", bytes).getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.readData("  Key1", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals(str, new String((byte[]) result.getData()));
                }
            });

        } catch (StorageException e) {
            fail("Failed to testReadPreserveKeyWithCallBack");
        }

    }


    @Test
    public void testReadPreserveData() {
        Log.d(TAG, "testReadPreserveData");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "  TestString  ";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("Key1", bytes);

            result = currentStorage.readData("Key1");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals(str, new String((byte[]) result.getData()));

        } catch (StorageException e) {
            fail("Failed to testReadPreserveData");
        }

    }


    @Test
    public void testReadPreserveDataWithCallBack() {
        Log.d(TAG, "testReadPreserveDataWithCallBack");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        final String str = "  TestString  ";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("Key1", bytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.readData("Key1", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals(str, new String((byte[]) result.getData()));
                    }
                });
            } else {
                fail("Precondition WRITE failed");
            }


        } catch (StorageException e) {
            fail("Failed to testReadPreserveDataWithCallBack");
        }
    }

    @Test
    public void testReadCaseSensitiveKey() {
        Log.d(TAG, "testReadCaseSensitiveKey");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("key1", bytes);

            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readData("Key");
                assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                if (result.getData() instanceof StorageException) {
                    StorageException exception = (StorageException) result.getData();
                } else {
                    fail("Result object is not of the type StorageException");
                }
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("Failed to testReadCaseSensitiveKey");
        }
    }

    @Test
    public void testReadCaseSensitiveKeyWithCallBack() {
        Log.d(TAG, "testReadCaseSensitiveKeyWithCallBack");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String str = "TestString";
        byte[] bytes = str.getBytes();
        try {
            result = currentStorage.writeData("key1", bytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.readData("Key1", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                        if (result.getData() instanceof StorageException) {
                            StorageException exception = (StorageException) result.getData();
                        } else {
                            fail("Result object is not of the type StorageException");
                        }
                    }
                });
            } else {
                fail("Precondition WRITE failed");
            }
        } catch (StorageException e) {
            fail("Failed to testReadCaseSensitiveKeyWithCallBack");
        }

    }

    /**
     * Tests to READ string from the KeyStore Storage
     */

    @Test
    public void testReadStringSuccess() {
        Log.d(TAG, "testReadStringSuccess");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("TestString", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringSuccessWithCallBack() {
        Log.d(TAG, "testReadStringSuccessWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.readString("Key", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("TestString", result.getData());
                    }
                });
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringFail() {
        Log.d(TAG, "testReadStringFail");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.readString("Key");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            if (result.getData() instanceof StorageException) {
                StorageException exception = (StorageException) result.getData();
            } else {
                fail("result is not of the type StorageException");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringFailWithCallBack() {
        Log.d(TAG, "testReadStringFailWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            currentStorage.readString("Key", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                    if (result.getData() instanceof StorageException) {
                        StorageException exception = (StorageException) result.getData();
                    } else {
                        fail("result is not of the type StorageException");
                    }
                }
            });

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringNilKey() {
        Log.d(TAG, "testReadStringNilKey");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            currentStorage.readString(null);
            fail("testReadStringNilKey should throw exception");

        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testReadStringNilKeyWithCallBack() {
        Log.d(TAG, "testReadStringNilKeyWithCallBack");
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.readString(null, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("testReadStringNilKey should throw exception");
                }
            });

        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testReadStringEmptyKey() {
        Log.d(TAG, "testReadStringEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("TestString", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringEmptyKeyWithCallBack() {
        Log.d(TAG, "testReadStringEmptyKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("TestString", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringEmptyData() {
        Log.d(TAG, "testReadStringEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringEmptyDataWithCallBack() {
        Log.d(TAG, "testReadStringEmptyDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringPreserveKey() {
        Log.d(TAG, "testReadStringPreserveKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("  Key ", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("  Key ");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("TestString", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringPreserveKeyWithCallBack() {
        Log.d(TAG, "testReadStringPreserveKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("  Key ", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("  Key ");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("TestString", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringPreserveData() {
        Log.d(TAG, "testReadStringPreserveData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "  TestString  ");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("  TestString  ", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringPreserveDataWithCallBack() {
        Log.d(TAG, "testReadStringPreserveDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "  TestString  ");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("  TestString  ", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringCaseSensitiveKey() {
        Log.d(TAG, "testReadStringCaseSensitiveKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                if (result.getData() instanceof StorageException) {
                    StorageException exception = (StorageException) result.getData();
                } else {
                    fail("result is not of the type StorageException");
                }
            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }

    @Test
    public void testReadStringCaseSensitiveKeyWithCallBack() {
        Log.d(TAG, "testReadStringCaseSensitiveKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        try {
            result = currentStorage.writeString("Key", "TestString");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.readString("key", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                        if (result.getData() instanceof StorageException) {
                            StorageException exception = (StorageException) result.getData();
                        } else {
                            fail("result is not of the type StorageException");
                        }
                    }
                });

            } else {
                fail("Precondition WRITE failed");
            }

        } catch (StorageException e) {
            fail("failed to Read String");
        }
    }


    /**
     * See if Delete all succeeds, all ways
     */
    @Test
    public void testDeleteAll() {
        Log.d(TAG, "testDeleteAll");
        StorageResult result;
        try {
            result = currentStorage.deleteAll();
            assertEquals(StorageResult.StorageOperationType.DELETE_ALL, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
        } catch (Exception e) {
            fail("Delete All failed");
        }
    }

    @Test
    public void testDeleteAllWithCallBack() {
        Log.d(TAG, "testDeleteAllWithCallBack");
        try {
            currentStorage.deleteAll(new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.DELETE_ALL, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                }
            });
        } catch (Exception e) {
            fail("testDeleteAllWithCallBack failed");
        }
    }

    @Test
    public void testDeleteSuccess() {
        Log.d(TAG, "testDeleteSuccess");
        StorageResult result;
        try {
            assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            String str = "TestString";
            byte[] bytes = str.getBytes();

            result = currentStorage.writeData("Key1", bytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.deleteData("Key1");
                assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Precondition WRITE failed");
            }


        } catch (Exception e) {
            fail("testDeleteSuccess failed");
        }
    }

    @Test
    public void testDeleteSuccessWithCallBack() {
        Log.d(TAG, "testDeleteSuccessWithCallBack");
        StorageResult result;
        try {
            assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            String str = "TestString";
            byte[] bytes = str.getBytes();

            result = currentStorage.writeData("Key1", bytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {

                currentStorage.deleteData("Key1", new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Precondition WRITE failed");
            }


        } catch (Exception e) {
            fail("Failed to testDeleteSuccessWithCallBack");
        }
    }

    @Test
    public void testDeleteFailure() {
        Log.d(TAG, "testDeleteFailure");
        StorageResult result;
        try {
            assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            result = currentStorage.deleteData("Key1");
            assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            if (result.getData() instanceof StorageException) {
                StorageException exception = (StorageException) result.getData();
                assertEquals(exception.getCode(), StorageException.READ_DATA_NOT_FOUND);
            } else {
                fail("testDeleteFailure failed: Result not an Exception");
            }

        } catch (StorageException e) {
            fail("Failed to testDeleteFailure");
        }

    }

    @Test
    public void testDeleteFailureWithCallBack() {
        Log.d(TAG, "testDeleteFailureWithCallBack");
        StorageResult result;
        try {
            assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
            currentStorage.deleteData("Key1", new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                    if (result.getData() instanceof StorageException) {
                        StorageException exception = (StorageException) result.getData();
                        assertEquals(exception.getCode(), StorageException.READ_DATA_NOT_FOUND);
                    } else {
                        fail("testDeleteFailure failed: Result data is not an Exception ");
                    }
                }
            });
        } catch (StorageException e) {
            fail("Failed to testDeleteFailureWithCallBack");
        }

    }

    @Test
    public void testDeleteWithNilKey() {
        Log.d(TAG, "testDeleteWithNilKey");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.deleteData(null);
            fail("testDeleteWithNilKey should throw exception");
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testDeleteWithNilKeyWithCallBack() {
        Log.d(TAG, "testDeleteWithNilKey");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            currentStorage.deleteData(null, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    fail("testDeleteWithNilKeyWithCallBack should throw exception");
                }
            });
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }

    }

    @Test
    public void testUpdateSuccess() {
        Log.d(TAG, "testUpdateSuccess");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateData("Key1", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateSuccess");
        }

    }

    @Test
    public void testUpdateSuccessWithCallBack() {
        Log.d(TAG, "testUpdateSuccessWithCallBack");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateSuccessWithCallBack");
        }

    }

    @Test
    public void testUpdateFail() {
        Log.d(TAG, "testUpdateFail");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.updateData("Key1", updateStrBytes);
            assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            if (result.getData() instanceof StorageException) {
                StorageException exception = (StorageException) result.getData();
            } else {
                fail("result data is not of the type StorageException");
            }

        } catch (StorageException e) {
            fail("Failed to testUpdateFail");
        }
    }

    @Test
    public void testUpdateFailWithCallBack() {
        Log.d(TAG, "testUpdateFailWithCallBack");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            currentStorage.updateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                    if (result.getData() instanceof StorageException) {
                        StorageException exception = (StorageException) result.getData();
                    } else {
                        fail("result data is not of the type StorageException");
                    }
                }
            });
        } catch (StorageException e) {
            fail("Failed to testUpdateFailWithCallBack");
        }
    }

    @Test
    public void testUpdateNilKey() {
        Log.d(TAG, "testUpdateNilKey");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateData(null, updateStrBytes);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testUpdateNilKeyWithCallBack() {
        Log.d(TAG, "testUpdateNilKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateData(null, updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testUpdateNilData() {
        Log.d(TAG, "testUpdateNilData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateData("Key1", null);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testUpdateNilDataWithCallBack() {
        Log.d(TAG, "testUpdateNilDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateData("Key1", null, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testUpdateWithEmptyData() {
        Log.d(TAG, "testUpdateWithEmptyData");
        StorageResult result;

        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateData("Key1", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateWithEmptyData");
        }

    }

    @Test
    public void testUpdateWithEmptyDataWithCallBack() {
        Log.d(TAG, "testUpdateSuccessWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateWithEmptyDataWithCallBack");
        }
    }

    @Test
    public void testUpdateEmptyKey() {
        Log.d(TAG, "testUpdateEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateData("", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Precondition write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateEmptyKey");
        }
    }

    @Test
    public void testUpdateEmptyKeyWithCallBack() {
        Log.d(TAG, "testUpdateEmptyKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateData("", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("", result.getData());
                    }
                });
            } else {
                fail("Precondition write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateEmptyKeyWithCallBack");
        }
    }


    @Test
    public void testUpdateStringSuccess() {
        Log.d(TAG, "testUpdateStringSuccess");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateString("Key1", updateStr);
                assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateSuccess");
        }
    }

    @Test
    public void testUpdateStringSuccessWithCallBack() {
        Log.d(TAG, "testUpdateStringSuccessWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateString("Key1", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateSuccessWithCallBack");
        }
    }

    @Test
    public void testUpdateStringFailure() {
        Log.d(TAG, "testUpdateStringFailure");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";

        try {
            result = currentStorage.updateString("Key1", updateStr);
            assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            if (result.getData() instanceof StorageException) {
                StorageException exception = (StorageException) result.getData();
                assertEquals(exception.getCode(), StorageException.READ_DATA_NOT_FOUND);
            } else {
                fail("result data is not of the type StorageException");
            }

        } catch (StorageException e) {
            fail("Failed to testUpdateFail");
        }
    }

    @Test
    public void testUpdateStringFailureWithCallBack() {
        Log.d(TAG, "testUpdateStringFailureWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String updateStr = "UpdateString";

        try {
            currentStorage.updateString("Key1", updateStr, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
                    if (result.getData() instanceof StorageException) {
                        StorageException exception = (StorageException) result.getData();
                        assertEquals(exception.getCode(), StorageException.READ_DATA_NOT_FOUND);
                    } else {
                        fail("result data is not of the type StorageException");
                    }
                }
            });
        } catch (StorageException e) {
            fail("Failed to testUpdateFailWithCallBack");
        }
    }

    @Test
    public void testUpdateStringNilKey() {
        Log.d(TAG, "testUpdateStringNilKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateString(null, updateStr);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }

    }

    @Test
    public void testUpdateStringNilKeyWithCallBack() {
        Log.d(TAG, "testUpdateStringNilKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateString(null, updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testUpdateStringNilData() {
        Log.d(TAG, "testUpdateStringNilData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateString("Key1", null);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testUpdateStringNilDataWithCallBack() {
        Log.d(TAG, "testUpdateStringNilDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateString("Key1", null, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testUpdateStringWithEmptyData() {
        Log.d(TAG, "testUpdateStringWithEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateString("Key1", updateStr);
                assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateWithEmptyData");
        }
    }

    @Test
    public void testUpdateStringWithEmptyDataWithCallBack() {
        Log.d(TAG, "testUpdateStringWithEmptyDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateString("Key1", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateWithEmptyDataWithCallBack");
        }
    }

    @Test
    public void testUpdateStringEmptyKey() {
        Log.d(TAG, "testUpdateStringEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.updateString("", updateStr);
                assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateEmptyKey");
        }
    }

    @Test
    public void testUpdateStringEmptyKeyWithCallBack() {
        Log.d(TAG, "testUpdateStringEmptyKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.updateString("", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testUpdateEmptyKeyWithCallBack");
        }
    }


    @Test
    public void testWriteOrUpdateWithExistingData() {
        Log.d(TAG, "testWriteOrUpdateWithExistingData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateData("Key1", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithExistingData");
        }
    }

    @Test
    public void testWriteOrUpdateWithExistingDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateWithExistingDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithExistingDataWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateWithNonExistingData() {
        Log.d(TAG, "testWriteOrUpdateWithNonExistingData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeOrUpdateData("Key1", updateStrBytes);
            assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData());
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithNonExistingData");
        }
    }

    @Test
    public void testWriteOrUpdateWithNonExistingDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateWithNonExistingDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {

            currentStorage.writeOrUpdateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key1", result.getData());
                }
            });

        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithNonExistingDataWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateEmptyKey() {
        Log.d(TAG, "testWriteOrUpdateEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateData("", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateEmptyKey");
        }
    }

    @Test
    public void testWriteOrUpdateEmptyKeyWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateEmptyKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData("", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateEmptyKeyWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateWithEmptyData() {
        Log.d(TAG, "testWriteOrUpdateWithEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateData("Key1", updateStrBytes);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithEmptyData");
        }
    }

    @Test
    public void testWriteOrUpdateWithEmptyDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateWithEmptyDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData("Key1", updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateWithEmptyDataWithCallBack");
        }
    }


    @Test
    public void testWriteOrUpdateNilKey() {
        Log.d(TAG, "testWriteOrUpdateNilKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateData(null, updateStrBytes);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testWriteOrUpdateNilKeyWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateNilKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();
        String updateStr = "UpdateString";
        byte[] updateStrBytes = updateStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData(null, updateStrBytes, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testWriteOrUpdateNilData() {
        Log.d(TAG, "testWriteOrUpdateNilData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateData("Key1", null);
                fail("test update for nil Data should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteOrUpdateNilDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateNilDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        byte[] writeStrBytes = writeStr.getBytes();

        try {
            result = currentStorage.writeData("Key1", writeStrBytes);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData("Key1", null, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil Data should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }


    /**
     * WRITEORUPDATE String from the STORAGE
     */

    @Test
    public void testWriteOrUpdateStringWithExistingData() {
        Log.d(TAG, "testWriteOrUpdateStringWithExistingData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateString("Key1", updateStr);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithExistingData");
        }
    }

    @Test
    public void testWriteOrUpdateStringWithExistingDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringWithExistingDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateString("Key1", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithExistingDataWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateStringWithNonExistingData() {
        Log.d(TAG, "testWriteOrUpdateStringWithNonExistingData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";
        try {
            result = currentStorage.writeOrUpdateString("Key1", updateStr);
            assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData());
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithNonExistingData");
        }
    }

    @Test
    public void testWriteOrUpdateStringWithNonExistingDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringWithNonExistingDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String updateStr = "UpdateString";
        try {

            currentStorage.writeOrUpdateString("Key1", updateStr, new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    assertEquals("Key1", result.getData());
                }
            });

        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithNonExistingDataWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateStringEmptyKey() {
        Log.d(TAG, "testWriteOrUpdateStringEmptyKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateString("", updateStr);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringEmptyKey");
        }
    }

    @Test
    public void testWriteOrUpdateStringEmptyKeyWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringEmptyKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateString("", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringEmptyKeyWithCallBack");
        }
    }

    @Test
    public void testWriteOrUpdateStringWithEmptyData() {
        Log.d(TAG, "testWriteOrUpdateStringWithEmptyData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateString("Key1", updateStr);
                assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("Key1", result.getData());
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithEmptyData");
        }
    }

    @Test
    public void testWriteOrUpdateStringWithEmptyDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringWithEmptyDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "";
        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateString("Key1", updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        assertEquals("Key1", result.getData());
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteOrUpdateStringWithEmptyDataWithCallBack");
        }
    }


    @Test
    public void testWriteOrUpdateStringNilKey() {
        Log.d(TAG, "testWriteOrUpdateStringNilKey");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);

        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateString(null, updateStr);
                fail("test update for nil key should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }

    }

    @Test
    public void testWriteOrUpdateStringNilKeyWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringNilKeyWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        String updateStr = "UpdateString";

        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateString(null, updateStr, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil key should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_KEY, e.getCode());
        }
    }

    @Test
    public void testWriteOrUpdateStringNilData() {
        Log.d(TAG, "testWriteOrUpdateStringNilData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeOrUpdateString("Key1", null);
                fail("test update for nil Data should throw exception");
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }

    @Test
    public void testWriteOrUpdateStringNilDataWithCallBack() {
        Log.d(TAG, "testWriteOrUpdateStringNilDataWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String writeStr = "TestString";
        try {
            result = currentStorage.writeString("Key1", writeStr);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                currentStorage.writeOrUpdateData("Key1", null, new StorageResultReceiver(null) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        fail("test update for nil Data should throw exception");
                    }
                });
            } else {
                fail("Initial write failed");
            }
        } catch (StorageException e) {
            assertEquals(StorageException.INVALID_INPUT_VALUE, e.getCode());
        }
    }


    @Test
    public void testAllKeysEmptyStore() {
        Log.d(TAG, "testAllKeysEmptyStore");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        result = currentStorage.getAllKeys();
        assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
        if (result.getData() instanceof ArrayList) {
            ArrayList allKeys = (ArrayList) result.getData();
            assertNotNull(allKeys);
            assertEquals(0, allKeys.size());
        } else {
            fail("Result is not a instance of type ArrayList");
        }

    }

    @Test
    public void testAllKeysEmptyStoreWithCallBack() {
        Log.d(TAG, "testAllKeysEmptyStoreWithCallBack");
        StorageResult result;

        try {
            currentStorage.getAllKeys(new StorageResultReceiver(null) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
                    assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                    if (result.getData() instanceof ArrayList) {
                        ArrayList allKeys = (ArrayList) result.getData();
                        assertNotNull(allKeys);
                        assertEquals(0, allKeys.size());
                    } else {
                        fail("Result is not a instance of type ArrayList");
                    }

                }
            });
        } catch (StorageException e) {
            fail("Failed to testAllKeysEmptyStoreWithCallBack");
        }
    }

    @Test
    public void testAllKeysNonEmptyStore() {
        Log.d(TAG, "testAllKeysNonEmptyStore");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String string1 = "TestString1";
        byte[] value1 = string1.getBytes();
        String string2 = "TestString2";
        byte[] value2 = string2.getBytes();
        String string3 = "TestString3";
        byte[] value3 = string3.getBytes();
        try {
            result = currentStorage.writeData("Key1", value1);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeData("Key2", value2);
                if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                    result = currentStorage.writeData("Key3", value3);
                    if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {

                        result = currentStorage.getAllKeys();
                        assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
                        assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                        if (result.getData() instanceof ArrayList) {
                            ArrayList allKeys = (ArrayList) result.getData();
                            assertNotNull(allKeys);
                            assertEquals(3, allKeys.size());
                        } else {
                            fail("Result is not a instance of type ArrayList");
                        }
                    } else {
                        fail("Precondition WRITE value3 failed");

                    }


                } else {
                    fail("Precondition WRITE value2 failed");

                }


            } else {
                fail("Precondition WRITE value1 failed");
            }

        } catch (StorageException e) {
            fail("Failed to testAllKeysNonEmptyStore");
        }
    }

    @Test
    public void testAllKeysNonEmptyStoreWithCallBack() {
        Log.d(TAG, "testAllKeysNonEmptyStoreWithCallBack");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        String string1 = "TestString1";
        byte[] value1 = string1.getBytes();
        String string2 = "TestString2";
        byte[] value2 = string2.getBytes();
        String string3 = "TestString3";
        byte[] value3 = string3.getBytes();
        try {

            result = currentStorage.writeData("Key1", value1);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.writeData("Key2", value2);
                if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                    result = currentStorage.writeData("Key3", value3);
                    if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {

                        currentStorage.getAllKeys(new StorageResultReceiver(null) {
                            @Override
                            public void onReceiveResult(StorageResult result) {
                                assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
                                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                                if (result.getData() instanceof ArrayList) {
                                    ArrayList allKeys = (ArrayList) result.getData();
                                    assertNotNull(allKeys);
                                    assertEquals(3, allKeys.size());
                                } else {
                                    fail("Result is not a instance of type ArrayList");
                                }
                            }
                        });
                    } else {
                        fail("Precondition WRITE value3 failed");
                    }
                } else {
                    fail("Precondition WRITE value2 failed");

                }

            } else {
                fail("Precondition WRITE value1 failed");
            }


        } catch (StorageException e) {
            fail("Failed to testAllKeysNonEmptyStoreWithCallBack");
        }
    }


    @Test
    public void testWriteStringReadData() {
        Log.d(TAG, "testWriteStringReadData");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("Key", "abcd 測試");
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readData("Key");
                assertEquals(StorageResult.StorageOperationType.READ, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("abcd 測試", new String((byte[]) result.getData()));

            } else {
                fail("Precondition WRITE failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteStringReadData");
        }
    }


    @Test
    public void testWriteDataReadString() {
        Log.d(TAG, "testWriteDataReadString");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            byte[] writeData = "abcd 測試".getBytes();
            result = currentStorage.writeData("Key", writeData);
            if (StorageResult.StorageOperationStatus.SUCCESS == result.getStatus()) {
                result = currentStorage.readString("Key");
                assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
                assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
                assertEquals("abcd 測試", result.getData());

            } else {
                fail("Precondition WRITE failed");
            }
        } catch (StorageException e) {
            fail("Failed to testWriteDataReadString");
        }
    }


    @Test
    public void testWriteBasicOperation() {
        Log.d(TAG, "testWriteBasicOperation");
        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            byte[] writeData = "abcd 測試".getBytes();
            result = currentStorage.writeData("Key1", writeData);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData());


            result = currentStorage.readString("Key1");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("abcd 測試", result.getData());

            byte[] writeData1 = "TestString".getBytes();
            result = currentStorage.writeData("Key2", writeData1);
            assertEquals(StorageResult.StorageOperationType.WRITE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key2", result.getData());

            result = currentStorage.getAllKeys();
            assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            if (result.getData() instanceof ArrayList) {
                ArrayList allKeys = (ArrayList) result.getData();
                assertEquals(2, allKeys.size());
            } else {
                fail("result is not of the type ArrayList");
            }

            result = currentStorage.deleteData("Key1");
            assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());

            result = currentStorage.getAllKeys();
            assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            if (result.getData() instanceof ArrayList) {
                ArrayList allKeys = (ArrayList) result.getData();
                assertEquals(1, allKeys.size());
            } else {
                fail("result is not of the type ArrayList");
            }


            result = currentStorage.readData("Key2");
            assertEquals(StorageResult.StorageOperationType.READ, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("TestString", new String((byte[]) result.getData()));


            result = currentStorage.writeOrUpdateString("Key2", "Value3");
            assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key2", result.getData());

            result = currentStorage.readString("Key2");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Value3", result.getData());

            result = currentStorage.updateData("Key2", "Value2".getBytes());
            assertEquals(StorageResult.StorageOperationType.UPDATE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key2", result.getData());

            result = currentStorage.writeOrUpdateString("Key3", "Value3");
            assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key3", result.getData());

            result = currentStorage.writeOrUpdateData("Key1", "Value1".getBytes());
            assertEquals(StorageResult.StorageOperationType.WRITE_OR_UPDATE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData());

            result = currentStorage.getAllKeys();
            assertEquals(StorageResult.StorageOperationType.GET_ALL_KEYS, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            if (result.getData() instanceof ArrayList) {
                ArrayList allKeys = (ArrayList) result.getData();
                assertEquals(3, allKeys.size());
            } else {
                fail("result is not of the type ArrayList");
            }

        } catch (StorageException e) {
            fail("Failed to testWriteBasicOperation");
        }

    }


    @Test
    public void testWriteCaseSensitiveKey() {
        Log.d(TAG, "testWriteCaseSensitiveKey");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("key1", "value1");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("key1", result.getData());

            result = currentStorage.writeString("Key1", "Value1");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Key1", result.getData());

            result = currentStorage.readString("key1");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("value1", result.getData());

            result = currentStorage.readString("Key1");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("Value1", result.getData());


        } catch (StorageException e) {

            fail("Failed to testWriteCaseSensitiveKey");

        }
    }


    @Test
    public void testWriteSubstringKey() {
        Log.d(TAG, "testWriteCaseSensitiveKey");

        StorageResult result;
        assertEquals("precondition DELETEALL failed", currentStorage.deleteAll().getStatus(), StorageResult.StorageOperationStatus.SUCCESS);
        try {
            result = currentStorage.writeString("keystone", "value1");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("keystone", result.getData());

            result = currentStorage.writeString("key", "value2");
            assertEquals(StorageResult.StorageOperationType.WRITE_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("key", result.getData());

            result = currentStorage.readString("key");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());
            assertEquals("value2", result.getData());

            result = currentStorage.deleteData("key");
            assertEquals(StorageResult.StorageOperationType.DELETE, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.SUCCESS, result.getStatus());

            result = currentStorage.readString("key");
            assertEquals(StorageResult.StorageOperationType.READ_STRING, result.getType());
            assertEquals(StorageResult.StorageOperationStatus.FAILURE, result.getStatus());
            if (result.getData() instanceof StorageException) {
                StorageException exception = (StorageException) result.getData();
            } else {
                fail("result object is not of the type StorageException");
            }

        } catch (StorageException e) {
            fail("Failed to testWriteCaseSensitiveKey");
        }

    }


}
