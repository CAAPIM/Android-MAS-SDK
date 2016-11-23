/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

/**
 * <p><b>MASException</b> is a general exception wrapper used by the MAS SDK to
 * normalize protocol specific messaging implementations.</p>
 */
public class MASException extends Exception {

    /**
     * <b>Description:</b> No args constructor.
     */
    public MASException() {
    }

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param detailMessage - free form message.
     */
    public MASException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param throwable - the prior exception.
     */
    public MASException(Throwable throwable) {
        super(throwable);
    }

    /**
     * <b>Description:</b> Convenience constructor.
     *
     * @param detailMessage - free form message.
     * @param throwable - the prior exception.
     */
    public MASException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
