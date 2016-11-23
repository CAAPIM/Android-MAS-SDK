/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui.listener;

import android.app.Activity;


/**
 * Listener interface to invoke a callback to the calling activity on fragment open/close events.
 */
public interface MASFragmentAttachDismissListener {

    /**
     * This is the method which is used as a callback upon closing a fragment.
     *
     * @param activity
     * @param flagRequestProcessing This boolean value indicates if request is still processing. False by default.
     */
    void handleDialogClose(Activity activity, boolean flagRequestProcessing);

    /**
     * This is the method which is used as a callback upon opening a fragment.
     *
     * @param activity
     */
    void handleDialogOpen(Activity activity);
}