/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.http.MAGResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class MASResponseBody<T> extends MAGResponseBody<T> {

    /**
     * @return Return a new ResponseBody with byte[] content.
     */
    public static MASResponseBody<byte[]> byteArrayBody() {

        return new MASResponseBody<byte[]>() {

            @Override
            public byte[] getContent() {
                return buffer;
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link JSONObject} content.
     */
    public static MASResponseBody<JSONObject> jsonBody() {

        return new MASResponseBody<JSONObject>() {

            @Override
            public JSONObject getContent() {
                try {
                    return new JSONObject(new String(buffer));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link String} content.
     */
    public static MASResponseBody<String> stringBody() {

        return new MASResponseBody<String>() {

            @Override
            public String getContent() {
                return new String(buffer);
            }
        };
    }
}
