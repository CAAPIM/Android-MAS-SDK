/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.web;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ca.mas.foundation.util.FoundationUtil;

import org.json.JSONObject;

import java.util.Map;

/**
 * <p><b>WebServiceRequest</b> is a wrapper for a web service request. This is MAG server call that supports the operations;
 * <ul>
 *     <li>CREATE operation. Successfully invoking this method will result in a resource being created at the server.</li>
 *     <li>READ operation. Successfully invoking this method will return the results of the query method.</li>
 *     <li>UPDATE operation. Successfully invoking this method will result in an existing resource being updated on the server.</li>
 *     <li>DELETE operation. Successfully invoking this method will result in an existing resource being deleted, or removed, from the server.</li>
 * </ul>
 * </p>
 */
public class WebServiceRequest {

    private static String TAG = WebServiceRequest.class.getSimpleName();

    private Uri mUri;
    private String mUserName;
    private String mPassword;
    private JSONObject mBody;
    private String mContentType;
    private String mEncoding;
    private Map<String, String> mHeaders = FoundationUtil.getStandardHeaders();

    /**
     * <b>Description:</b> Default constructor
     */
    public WebServiceRequest() {
    }

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param mUri used to make the web service call.
     */
    public WebServiceRequest(@NonNull Uri mUri) {
        this.mUri = mUri;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return Uri used in this request.
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return JSONObject representing the body of the web service response.
     */
    public JSONObject getBody() {
        return mBody;
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param body the JSONObject that will be sent to the MAG server to perform a web service call at the target specified.
     */
    public void setBody(JSONObject body) {
        this.mBody = body;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return String representing the  {@link <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>}
     * compliant MIME type.
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param contentType String representing the  {@link <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>}
     * compliant MIME type.
     */
    public void setContentType(String contentType) {
        this.mContentType = contentType;
    }

    /**
     * <b>Description:</b> Getter. By default this is UTF-8.
     *
     * @return String - a standard encoding string, such as 'UTF8'
     */
    public String getEncoding() {
        return mEncoding;
    }

    public void setEncoding(String encoding) {
        this.mEncoding = encoding;
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param userName used to authenticate the call.
     */
    public void setUserName(String userName) {
        mUserName = userName;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return - the user name.
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param password the user's password.
     */
    public void setPassword(String password) {
        mPassword = password;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return String - the user's password.
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @param uri representing the target of this operation.
     */
    public void setUri(@NonNull Uri uri) {
        this.mUri = uri;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return Map<String, String>
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * <b>Description:</b> Setter.
     *
     * @param headers the request headers for this request.
     */
    public void setHeaders(@NonNull Map<String, String> headers) {
        mHeaders = headers;
    }

    /**
     * <b>Description:</b> Add any headers to the request. These will be passed into the WebServiceClient prior to
     * making the request.
     *
     * @param key the header key such as 'Content-Type'.
     * @param value the header value such as 'application/octet-stream'.
     */
    public void addHeader(@NonNull String key, @NonNull String value) {
        mHeaders.put(key, value);
    }
}
