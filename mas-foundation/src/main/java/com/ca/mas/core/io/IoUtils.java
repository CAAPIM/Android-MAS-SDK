/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Utility methods for doing I/O.
 */
public class IoUtils {

    /**
     * Read the entirety of the specified InputStream into memory and return it as a byte array.
     * <p/>
     * The stream will be read until EOF but will not be closed by this method.
     * <p/>
     * This method will throw an IOException if the stream is longer than the specified limit.
     *
     * @param stream the stream to read.  Required.
     * @param limit the maximum number of bytes to read into memory.
     * @return the remaining contents of the stream as a byte array.  Never null.
     * @throws IOException if an IOException occurs while reading the stream, or if the stream length limit is exceeded.
     */
    public static byte[] slurpStream(InputStream stream, int limit) throws IOException {
        final byte[] buf = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        int got, total = 0;
        while ((got = stream.read(buf)) > 0) {
            out.write(buf, 0, got);
            total += got;
            if (total >= limit)
                throw new IOException("Stream length limit exceeded; limit=" + limit);
        }
        return out.toByteArray();
    }

    /**
     * Convert the specified binary data into a string containing hexadecimal digits.
     * Example:  hexDump(new byte[] { (byte)0xAB, (byte)0xCD }).equals("abcd")
     *
     * @param binaryData  the data to dump
     * @return the hex dump of the data
     */
    public static String hexDump(byte[] binaryData) {
        return hexDump(binaryData, 0, binaryData.length);
    }

    /**
     * Convert a portion of the specified binary data into a string containing hexadecimal digits.
     *
     * @param binaryData  the data to dump
     * @param off offset of first byte to include in the dump.
     * @param len number of bytes to include in the dump.
     * @return the hex dump of the data
     */
    public static String hexDump(byte[] binaryData, int off, int len) {
        return hexDump( binaryData, off, len, false );
    }

    /**
     * Convert a portion of the specified binary data into a string containing hexadecimal digits.
     *
     * @param binaryData  the data to dump
     * @param off offset of first byte to include in the dump.
     * @param len number of bytes to include in the dump.
     * @param upperCase true if the hex should use uppercase letters.
     * @return the hex dump of the data
     */
    public static String hexDump(byte[] binaryData, int off, int len, boolean upperCase) {
        final char[] hex = upperCase ? hexadecimal_upper : hexadecimal;
        if (binaryData == null) throw new NullPointerException();
        if (off < 0 || len < 0 || off + len > binaryData.length) throw new IllegalArgumentException();
        char[] buffer = new char[len * 2];
        for (int i = 0; i < len; i++) {
            int low = (binaryData[off + i] & 0x0f);
            int high = ((binaryData[off + i] & 0xf0) >> 4);
            buffer[i*2] = hex[high];
            buffer[i*2 + 1] = hex[low];
        }
        return new String(buffer);
    }

    /**
     * Base-64 encode the charset-encoded bytes of the specified string.
     *
     * @param str the string to encode.  Required.
     * @return the Base-64 encoded form of the charset representation of the string.  Never null.
     */
    public static String base64(String str, Charset charset) {
        return Base64.encodeToString(str.getBytes(charset), Base64.NO_WRAP);
    }

    /**
     * Close the specified closeable, if it is non-null.
     * <p/>
     * Any IOException will be logged at debug level.
     * Any other Exception will be logged as a warning.
     *
     * @param closeable a closeable or null
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "Exception closing closeable: " + e.getMessage(), e);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error closing closeable: " + e.getMessage(), e);
        }
    }

    private IoUtils() {
    }

    private static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] hexadecimal_upper = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
}
