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
 * <p>The <b>MASMeta</b> class implements the SCIM resourceType <a href="https://tools.ietf.org/html/rfc7643#section-3.1">meta</a> attribute.</p>
 */
public class MASMeta implements MASTransformable {

    private String mResourceType;
    private String mCreated;
    private String mLastModified;
    private String mVersion;
    private String mLocation;

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
            mResourceType = jobj.optString(IdentityConsts.KEY_META_RESOURCE_TYPE);
            mCreated = jobj.optString(IdentityConsts.KEY_META_CREATED);
            mLastModified = jobj.optString(IdentityConsts.KEY_META_LAST_MODIFIED);
            mVersion = jobj.optString(IdentityConsts.KEY_META_VERSION);
            mLocation = jobj.optString(IdentityConsts.KEY_META_LOCATION);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(IdentityConsts.KEY_META_RESOURCE_TYPE, mResourceType);
        jobj.put(IdentityConsts.KEY_META_CREATED, mCreated);
        jobj.put(IdentityConsts.KEY_META_LAST_MODIFIED, mLastModified);
        jobj.put(IdentityConsts.KEY_META_VERSION, mVersion);
        jobj.put(IdentityConsts.KEY_META_LOCATION, mLocation);
        return jobj;
    }

    /**
     * <b>Description:</b> The name of the resource type of the resource.  This attribute has a mutability of
     * "readOnly" and "caseExact" as "true".
     *
     * @return String representing a free-form resource type. This attribute may contain white-space or be null.
     */
    public String getResourceType() {
        return mResourceType;
    }

    /**
     * <b>Description:</b> The "DateTime" that the resource was added to the service
     * provider.  This attribute MUST be a DateTime.
     *
     * @return String the DateTime String. This attribute may contain white-space or be null.
     */
    public String getCreated() {
        return mCreated;
    }

    /**
     * <b>Description:</b> The most recent DateTime that the details of this resource were updated at the service provider.  If this
     * resource has never been modified since its initial creation, the value MUST be the same as the value of "created".
     *
     * @return String the DateTime String. This attribute may contain white-space or be null.
     */
    public String getLastModified() {
        return mLastModified;
    }

    /**
     * <b>Description:</b> The version of the resource being returned.  This value must be the same as the entity-tag
     * (ETag) HTTP response header.
     *
     * @return String the HTTP entity tag representing the resourceType version. This attribute may contain white-space or be null.
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * <b>Description:</b> The URI of the resource being returned.  This value MUST be the same as the "Content-Location"
     * HTTP response header.
     *
     * @return String representation of the Content-Location header. This attribute may contain white-space or be null.
     */
    public String getLocation() {
        return mLocation;
    }
}
