/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <table>
 * <caption>Summary</caption>
 * <thead>
 * <tr><th>Value</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>MASConstants.MAS_STATE_NOT_CONFIGURED</td><td>State that SDK has not been initialized and does not have configuration file.</td></tr>
 * <tr><td>MASConstants.MAS_STATE_NOT_INITIALIZED</td><td>State that SDK has the active configuration either in the local file system, but has not been initialized yet.</td></tr>
 * <tr><td>MASConstants.MAS_STATE_STARTED</td><td>State that SDK did start; at this state, SDK should be fully functional.</td></tr>
 * <tr><td>MASConstants.MAS_STATE_STOPPED</td><td>State that SDK did stopInternal; at this state, SDK is properly stopped and should be able to re-start.<td></tr>
 * </tbody>
 * </table>
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {MASConstants.MAS_STATE_NOT_CONFIGURED,
        MASConstants.MAS_STATE_NOT_INITIALIZED,
        MASConstants.MAS_STATE_STOPPED,
        MASConstants.MAS_STATE_STARTED})
public @interface MASState {
}
