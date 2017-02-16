/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import com.ca.mas.core.io.Charsets;

import java.nio.charset.Charset;

/**
 * Content type of HTTP request or response body
 */
public class ContentType {

    public static final ContentType APPLICATION_OCTET_STREAM = new ContentType("application/octet-stream", null );
    public static final ContentType APPLICATION_FORM_URLENCODED = new ContentType("application/x-www-form-urlencoded", Charsets.ISO_8859_1);
    public static final ContentType APPLICATION_JSON = new ContentType("application/json", Charsets.UTF8);
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain", Charsets.ISO_8859_1);


    private final String mimeType;
    private final Charset charset;

    public ContentType(String mimeType, Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
    }

    /**
     * @return The mime type of the request and response body
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return The charset of this content type
     */
    public Charset getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mimeType);
        if (this.charset != null) {
            sb.append("; charset=");
            sb.append(this.charset.name());
        }
        return sb.toString();
    }
}
