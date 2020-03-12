/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import androidx.annotation.IntDef;

import com.ca.mas.foundation.MASConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * <table>
 * <caption>Summary</caption>
 * <thead>
 * <tr><th>Value</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>MASConstants.MAS_USER,</td><td>Store the user profile. For example, if you want to extend the SCIM user profile. Data stored here can be shared across apps. If local storage, the data is accessible only by the user. </td></tr>
 * <tr><td>MASConstants.MAS_APPLICATION</td><td> Store the application configuration. For example, you can store a simple Message to use for apps. Data stored here is shared across users. </td></tr>
 * <tr><td>MASConstants.MAS_USER | MASConstants.MAS_APPLICATION</td><td>Store the application state for the user. For example, you can store a game score or game state. If local storage, the app state is accessible only by the user</td></tr>
 * </tbody>
 * </table>
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(flag = true, value = {MASConstants.MAS_USER, MASConstants.MAS_APPLICATION})
public @interface MASStorageSegment {
}
