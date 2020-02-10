/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.ca.mas.foundation.Internal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class that provides the some default DataMarshaller's that could be used by reused by
 * {@link com.ca.mas.storage.MASStorage} implementation(s).
 *
 */
abstract class AbstractMASStorage implements MASStorage {

    private List<DataMarshaller> marshallers = new ArrayList<>();

    @Internal
    protected DataMarshaller findMarshaller(Object type) {
        for (DataMarshaller current : marshallers) {
            if (current.getType().isAssignableFrom(type.getClass())) {
                return current;
            }

        }
        throw new TypeNotPresentException(type.getClass().getName(), null);
    }

    @Internal
    protected DataMarshaller findMarshaller(String type) {
        for (DataMarshaller current : marshallers) {
            if (current.getTypeAsString().equals(type)) {
                return current;
            }
        }
        throw new TypeNotPresentException(type, null);
    }


    public class BitmapDataMarshaller implements DataMarshaller<Bitmap> {

        @Override
        public Bitmap unmarshall(byte[] content) throws Exception {
            return BitmapFactory.decodeByteArray(content, 0, content.length);
        }

        @Override
        public byte[] marshall(Bitmap data) throws Exception {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            data.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }

        @Override
        public Class<Bitmap> getType() {
            return Bitmap.class;
        }

        @Override
        public String getTypeAsString() {
            return "image/png";
        }
    }


    public class ByteArrayDataMarshaller implements DataMarshaller<byte[]> {

        @Override
        public byte[] unmarshall(byte[] content) throws Exception {
            return content;
        }

        @Override
        public byte[] marshall(byte[] data) throws Exception {
            return data;
        }

        @Override
        public Class<byte[]> getType() {
            return byte[].class;
        }

        @Override
        public String getTypeAsString() {
            return "application/octet-stream";
        }
    }

    /**
     * Converts a Serializable to byte[] and back.
     */
    public static class ParcelDataMarshaller implements DataMarshaller<Parcel> {

        //public String typeString = "parcelable";

        @Override
        public Parcel unmarshall(byte[] content) {
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(content, 0, content.length);
            parcel.setDataPosition(0);
            return parcel;
        }

        @Override
        public byte[] marshall(@NonNull Parcel data) {

            byte[] bytes = data.marshall();
            data.recycle();
            return bytes;

        }

        @Override
        public Class<Parcel> getType() {
            return Parcel.class;
        }

        @Override
        public String getTypeAsString() {
            return "parcel";
        }
    }

    /**
     * Converts a Serializable to byte[] and back.
     */
    public static class SerializableDataMarshaller implements DataMarshaller<Serializable> {

        @Override
        public Serializable unmarshall(byte[] content) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            ObjectInput in = null;
            try {
                in = new ObjectInputStream(bis);
                return (Serializable) in.readObject();
            } finally {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public byte[] marshall(@NonNull Serializable data) throws IOException {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(data);
                return bos.toByteArray();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ignored) {
                }
                try {
                    bos.close();
                } catch (IOException ignored) {
                }
            }

        }

        @Override
        public Class<Serializable> getType() {
            return Serializable.class;
        }

        @Override
        public String getTypeAsString() {
            return "serializable";
        }
    }

    /**
     * Converts a JSONObject to byte[] and back.
     */
    public class JsonDataMarshaller implements DataMarshaller<JSONObject> {

        @Override
        public JSONObject unmarshall(@NonNull byte[] content) throws UnsupportedEncodingException, JSONException {

            return new JSONObject(new String(content, "UTF-8"));

        }

        @Override
        public byte[] marshall(@NonNull JSONObject data) throws UnsupportedEncodingException {
            return data.toString().getBytes("UTF-8");
        }

        @Override
        public Class<JSONObject> getType() {
            return JSONObject.class;
        }

        @Override
        public String getTypeAsString() {
            return "application/json";
        }
    }
    public class StringDataMarshaller implements DataMarshaller<String> {

        @Override
        public String unmarshall(byte[] content) throws Exception {
            return new String(content, "UTF-8");
        }

        @Override
        public byte[] marshall(@NonNull String data) throws Exception {
            return data.getBytes("UTF-8");
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String getTypeAsString() {
            return "text/plain";
        }
    }

    @Internal
    @Override
    public void register(@NonNull DataMarshaller marshaller) {
        marshallers.add(marshaller);
    }


    @Internal
    protected void checkNull(String key, Object value) {
        checkNull(key);
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
    }

    @Internal
    protected void checkNull(String key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
    }
}
