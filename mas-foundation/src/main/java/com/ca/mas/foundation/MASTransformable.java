/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p><b>MASTransformable</b> is implemented by classes that are populated via SCIM.
 * This interface solely contains the <i>populate</i> method for converting JSON responses
 * into object models.</p>
 */
public interface MASTransformable {
    /**
     * <b>Description:</b>
     * <p>Populates the implementing class with the contents of the JSONObject.
     * A SCIM response contains a JSONObject with a defined structure, but much of the data could
     * be missing from the response, such as unpopulated fields, etc. Implementers need to determine
     * what is available.</p>
     * Internal use only.
     *
     * @param jobj is the non-null JSON response from SCIM.
     * @throws JSONException is thrown when the JSONObject is invalid.
     */
    @Internal
    void populate(@NonNull JSONObject jobj) throws JSONException;

    /**
     * <b>Description:</b>
     * <p>This method takes the implementing entity and transforms its properties into a JSON document.
     * This is the reverse of the above populate method.</p>
     * Internal use only.
     *
     * @return JSONObject - a valid JSON document representation of the entity.
     * @throws JSONException - if any invalid components are encountered.
     */
    @Internal
    JSONObject getAsJSONObject() throws JSONException;
}
