/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <table>
 * <caption>Summary</caption>
 * <thead>
 * <tr><th>Value</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>MASConstants.MAS_STATE_STOPPED</td><td>State that SDK did stop; at this state, SDK is properly stopped and should be able to re-start.<td></tr>
 * </tbody>
 * </table>
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef(value = {MASClaims.CONTENT, MASClaims.CONTENT_TYPE})
public @interface MASClaimsConstants {
}
