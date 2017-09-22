/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

public interface DataMarshaller<T> {

    /**
     * Converts the raw bytes back to its original Type
     *
     * @param content
     * @return
     */
    T unmarshall(byte[] content) throws Exception;

    /**
     * Converts the data in to raw bytes
     *
     * @param data
     * @return
     */
    byte[] marshall(T data) throws Exception;

    /**
     * Determines the type of object
     *
     * @return
     */
    Class<T> getType();

    /**
     * The type value stored to the datasource
     *
     * @return
     */
    String getTypeAsString();
}
