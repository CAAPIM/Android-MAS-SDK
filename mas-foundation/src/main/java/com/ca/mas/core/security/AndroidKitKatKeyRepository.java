/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import androidx.annotation.RequiresApi;

import java.security.spec.RSAKeyGenParameterSpec;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class AndroidKitKatKeyRepository extends AndroidJellyBeanMR2KeyRepository {

    @Override
    KeyPairGeneratorSpec.Builder getKeyPairGeneratorSpecBuilder(String alias, GenerateKeyAttribute attributes) {
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(attributes.getKeySize(), RSAKeyGenParameterSpec.F4);
        return super.getKeyPairGeneratorSpecBuilder(alias, attributes).setAlgorithmParameterSpec(spec);
    }
}
