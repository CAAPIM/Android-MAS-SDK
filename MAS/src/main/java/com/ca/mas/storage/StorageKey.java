/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.MASTransformable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>The <b>StorageKey</b> is a representation of the key, modified date, and the reference to the actual storage item. The storage keys
 * can be retrieved by calling the read operation without the data key argument. The resultant document has the form;</p>
 * <pre>
 * {
 * "totalItems": "3",
 * "results": [
 * {
 * "key": "122",
 * "modifiedDate": "Mon Dec 21 18:29:14 PST 2015",
 * "$ref": "https:\/\/host.com\/MASS\/v1\/Client\/efa9a88c-fd24-4180-ac4d-0047f9962402\/Data\/122"
 * },
 * {
 * "key": "123",
 * "modifiedDate": "Mon Dec 21 18:29:08 PST 2015",
 * "$ref": "https:\/\/host.com\/MASS\/v1\/Client\/efa9a88c-fd24-4180-ac4d-0047f9962402\/Data\/123"
 * },
 * {
 * "key": "124",
 * "modifiedDate": "Mon Dec 21 18:29:11 PST 2015",
 * "$ref": "https:\/\/ssg90.l7tech.com:8443\/MASS\/v1\/Client\/efa9a88c-fd24-4180-ac4d-0047f9962402\/Data\/124"
 * }
 * ]
 * }
 * </pre>
 * <p>After retrieving the array of storage keys the <i>populateStorageKey</i> method creates the set of read-only values so that the
 * complete storage item can be retrieved on a read operation where the key is specified.</p>
 */
class StorageKey implements MASTransformable {

    private String dataKey;
    private String contentType;
    private String modifiedDate;
    private String reference;

    /**
     * <b>Description: </b> Return the unique key for this storage item. Creating a storage item with a null data key
     * is not permitted. Therefore, the value returned from this accessor will be non-null in all cases.
     *
     * @return String the non-null value that identifies this storage item.
     */
    public String getDataKey() {
        return dataKey;
    }

    /**
     * <b>Description: </b> getter for the URL of the specific item. This value is returned when a request for all items
     * is made to the cloud storage.
     *
     * @return String the '$ref' URL
     */
    public String getReference() {
        return reference;
    }

    /**
     * <b>Description: </b> The read-only accessor for the server created and updated date for this storage item. The
     * modified date and format is determined by the server and must not be altered by the client. There is no restriction
     * on this date and the time zone is based on the server's date management logic. The date is ambiguous in the sense
     * that there is no mechanism for identifying and verifying clock skew between the client and server.
     *
     * @return String the possibly null value identifying the last time this storage item was managed by the server.
     */
    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
        dataKey = jobj.optString(StorageConsts.KEY_KEY);
        modifiedDate = jobj.optString(StorageConsts.KEY_MODIFIED_DATE);
        contentType = jobj.optString(StorageConsts.KEY_TYPE);
        reference = jobj.optString(StorageConsts.KEY_REFERENCE);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject o = new JSONObject();
        o.putOpt(StorageConsts.KEY_KEY, dataKey);
        o.putOpt(StorageConsts.KEY_MODIFIED_DATE, modifiedDate);
        o.putOpt(StorageConsts.KEY_TYPE, contentType);
        o.putOpt(StorageConsts.KEY_REFERENCE, reference);
        return o;
    }
}
