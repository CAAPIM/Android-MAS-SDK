/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging.topic;

import android.support.annotation.IntDef;

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
 * <tr><td>MASConstants.MAS_USER,</td><td>Subscribe/Publish message to user.</td></tr>
 * <tr><td>MASConstants.MAS_APPLICATION</td><td>Subscribe/Publish message to application.</td></tr>
 * <tr><td>MASConstants.MAS_USER | MASConstants.MAS_APPLICATION</td><td>Subscribe/Publish message to user and application.</td></tr>
 * </tbody>
 * </table>
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(flag = true, value = {MASConstants.MAS_USER, MASConstants.MAS_APPLICATION})
public @interface MASMessagingSegment {
}
