/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>The <b>Attribute</b> class represents the {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">SCIM</a>}
 * attribute data model and is used to describe an attribute available in the
 * {@link <a href="https://tools.ietf.org/html/rfc7643">SCIM</a>} server implementation.</p>
 */
public class Attribute {

    private String mName;
    private String mType;
    private boolean mIsMultiValued;
    private String mDescription;
    private boolean mIsRequired;
    private boolean mIsCaseExact;
    private String mMutability;
    private String mReturned;
    private String mUniqueness;

    /**
     * <b>Description:</b> Populate the Attribute from the JSON object retrieved from the server.
     *
     * @param jobj the JSONObject
     * @throws JSONException if the JSON file cannot be processed.
     */
    public void populate(JSONObject jobj) throws JSONException {
        mName = jobj.optString("name");
        mType = jobj.optString("type");
        mIsMultiValued = jobj.optBoolean("multiValued");
        mDescription = jobj.optString("description");
        mIsRequired = jobj.optBoolean("required");
        mIsCaseExact = jobj.optBoolean("caseExact");
        mMutability = jobj.optString("mutability");
        mReturned = jobj.optString("returned");
        mUniqueness = jobj.optString("uniqueness");
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">description</a>}
     *
     * @return String the description of the attribute.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * <b>Description:</b> Predicate. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">caseExact</a>}.
     *
     * @return true or false.
     */
    public boolean isCaseExact() {
        return mIsCaseExact;
    }

    /**
     * <b>Description:</b> Predicate. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">multiValue</a>}
     *
     * @return true or false.
     */
    public boolean isMultiValued() {
        return mIsMultiValued;
    }

    /**
     * <b>Description:</b> Predicate. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">required</a>}.
     *
     * @return true or false. Is "false" by default (i.e., not REQUIRED).
     */
    public boolean isRequired() {
        return mIsRequired;
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">mutability</a>}
     *
     * @return String is "readWrite" (i.e., modifiable).
     */
    public String getMutability() {
        return mMutability;
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">name</a>}.
     *
     * @return String the name of the attribute.
     */
    public String getName() {
        return mName;
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">returned</a>}.
     *
     * @return String which is is "default" (the attribute value is returned by default)
     */
    public String getReturned() {
        return mReturned;
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">type</a>}.
     *
     * @return String is "string".
     */
    public String getType() {
        return mType;
    }

    /**
     * <b>Description:</b> Getter. See {@link <a href="https://tools.ietf.org/html/rfc7643#Section-2.1">uniqueness</a>}.
     *
     * @return String is "none" (has no uniqueness enforced).
     */
    public String getUniqueness() {
        return mUniqueness;
    }
}
