/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AndroidNKeyRepository extends AndroidMKeyRepository {

    @Override
    KeyGenParameterSpec.Builder getKeyGenParameterSpecBuilder(String alias, GenerateKeyAttribute attributes) {
        return super.getKeyGenParameterSpecBuilder(alias, attributes)
                .setInvalidatedByBiometricEnrollment(attributes.isInvalidatedByBiometricEnrollment());
    }
}
