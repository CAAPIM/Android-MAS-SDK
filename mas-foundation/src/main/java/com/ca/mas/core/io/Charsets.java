/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io;

import java.nio.charset.Charset;

/**
 * Holds some common Charset instances as static fields.
 */
public class Charsets {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final Charset ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO_8859_1");

    private Charsets() {
    }
}
