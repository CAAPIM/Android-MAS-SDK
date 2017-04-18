/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.MASTransformable;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>The <b>MASName</b> interface contains the common attribute {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">name</a>}
 * components of the user's name. Service providers MAY return just the full name as a single string in the formatted
 * sub-attribute, or they MAY return just the individual component attributes using the other sub-attributes, or they MAY return
 * both.</p>
 * <pre>
 *  "name": {
 *      "formatted": "Ms. Barbara J Jensen, III",
 *      "familyName": "Jensen",
 *      "givenName": "Barbara",
 *      "middleName": "Jane",
 *      "honorificPrefix": "Ms.",
 *      "honorificSuffix": "III"
 *  }
 * </pre>
 */
public class MASName implements MASTransformable {

    // name
    private String mFormatted;
    private String mFamilyName;
    private String mGivenName;
    private String mMiddleName;
    private String mHonorificPrefix;
    private String mHonorificSuffix;

    public MASName() {
    }

    /**
     * Convience constructor that takes the 2 components of the name that are the least likey to be null.
     *
     * @param givenName  - see {@link MASName#getGivenName()}
     * @param familyName - see {@link MASName#getFamilyName()}
     */
    public MASName(String givenName, String familyName) {
        mGivenName = givenName;
        mFamilyName = familyName;
    }

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
        mFormatted = jobj.optString(IdentityConsts.KEY_FORMATTED);
        mFamilyName = jobj.optString(IdentityConsts.KEY_FAMILY_NAME);
        mGivenName = jobj.optString(IdentityConsts.KEY_GIVEN_NAME);
        mMiddleName = jobj.optString(IdentityConsts.KEY_MIDDLE_NAME);
        mHonorificPrefix = jobj.optString(IdentityConsts.KEY_PREFIX_NAME);
        mHonorificSuffix = jobj.optString(IdentityConsts.KEY_SUFFIX_NAME);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(IdentityConsts.KEY_FORMATTED, mFormatted);
        jobj.put(IdentityConsts.KEY_FAMILY_NAME, mFamilyName);
        jobj.put(IdentityConsts.KEY_GIVEN_NAME, mGivenName);
        jobj.put(IdentityConsts.KEY_MIDDLE_NAME, mMiddleName);
        jobj.put(IdentityConsts.KEY_PREFIX_NAME, mHonorificPrefix);
        jobj.put(IdentityConsts.KEY_SUFFIX_NAME, mHonorificSuffix);
        return jobj;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">formatted</a>}
     *
     * @return String the formatted name of the user. This attribute may contain white-space or be null.
     */
    public String getFormatted() {
        return mFormatted;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">familyName</a>}
     *
     * @return String the family name of the user. This attribute may contain white-space or be null.
     */
    public String getFamilyName() {
        return mFamilyName;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">givenName</a>}
     *
     * @return String the given name. This attribute may contain white-space or be null.
     */
    public String getGivenName() {
        return mGivenName;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">middleName</a>}
     *
     * @return String the middle name of the user. This attribute may contain white-space or be null.
     */
    public String getMiddleName() {
        return mMiddleName;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">honorificPrefix</a>}
     *
     * @return String the honorific prefix of the user's name. This attribute may contain white-space or be null.
     */
    public String getHonorificPrefix() {
        return mHonorificSuffix;
    }

    /**
     * <b>Description:</b> See the SCIM definition of {@link <a href="https://tools.ietf.org/html/rfc7643#section-4.1.1">honorificSuffix</a>}
     *
     * @return String the honorificSuffix of the user's name. This attribute may contain white-space or be null.
     */
    public String getHonorificSuffix() {
        return mHonorificPrefix;
    }

}
