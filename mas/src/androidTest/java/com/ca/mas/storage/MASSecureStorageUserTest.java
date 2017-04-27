/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.storage;

import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.foundation.MASConstants;

import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;

public class MASSecureStorageUserTest extends MASStorageTest {

    @Override
    int getMode() {
        return MASConstants.MAS_USER;
    }

    @Override
    MASStorage getMASStorage() {
        MASStorage storage = new MASSecureStorage();
        storage.register(new AbstractMASStorage.SerializableDataMarshaller());
        storage.register(new AbstractMASStorage.ParcelDataMarshaller());
        return storage;
    }

    @Test
    public void testSerializable() throws Exception {

        String key = "key";
        final TestSerializableObject expectedValue = new TestSerializableObject("TEST1", "TEST2");

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(key, expectedValue, getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<TestSerializableObject> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey(key, getMode(), findByKeyCallbackFuture);
        assertEquals(expectedValue.name, findByKeyCallbackFuture.get().name);
        assertEquals(expectedValue.value, findByKeyCallbackFuture.get().value);

    }

    @Test
    public void testParcelableObject() throws Exception {

        String key = "key";
        final TestParcelableObject expectedValue = new TestParcelableObject("TEST1", "TEST2");

        Parcel parcel = Parcel.obtain();
        expectedValue.writeToParcel(parcel, 0);

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(key, parcel, getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<Parcel> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey(key, getMode(), findByKeyCallbackFuture);
        Parcel retrievedData = findByKeyCallbackFuture.get();

        TestParcelableObject result = TestParcelableObject.CREATOR.createFromParcel(retrievedData);

        assertEquals(expectedValue.name, result.name);
        assertEquals(expectedValue.value, result.value);

    }

    private static class TestSerializableObject implements Serializable {
        private String name;
        private String value;

        public TestSerializableObject(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class TestParcelableObject implements Parcelable {

        private String name;
        private String value;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.value);
        }

        public TestParcelableObject(String name, String value) {
            this.name = name;
            this.value = value;
        }

        protected TestParcelableObject(Parcel in) {
            this.name = in.readString();
            this.value = in.readString();
        }

        public static final Creator<TestParcelableObject> CREATOR = new Creator<TestParcelableObject>() {
            @Override
            public TestParcelableObject createFromParcel(Parcel source) {
                return new TestParcelableObject(source);
            }

            @Override
            public TestParcelableObject[] newArray(int size) {
                return new TestParcelableObject[size];
            }
        };
    }



}
