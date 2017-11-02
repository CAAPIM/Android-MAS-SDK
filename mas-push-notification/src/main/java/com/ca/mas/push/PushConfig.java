/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;

import com.ca.mas.foundation.MASConstants;

class PushConfig {

    boolean registerOnStartUp = true;
    int grantType = MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS;

    public PushConfig(Context context) {
        try {
            ComponentName componentName = new ComponentName(context, MASPushContentProvider.class);
            ProviderInfo providerInfo = context.getPackageManager().getProviderInfo(componentName, PackageManager.GET_META_DATA);
            registerOnStartUp = providerInfo.metaData.getBoolean("register-on-start", true);
            grantType = providerInfo.metaData.getInt("grant-type", MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS);
        } catch (PackageManager.NameNotFoundException e) {
            //ignore
        }
    }

    public boolean isRegisterOnStartUp() {
        return registerOnStartUp;
    }

    public int getGrantType() {
        return grantType;
    }
}
