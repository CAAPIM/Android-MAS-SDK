/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;

import com.ca.mas.foundation.MAS;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class AndroidKitKatKeyRepository extends AndroidJellyBeanMR2KeyRepository {

    @Override
    KeyPairGeneratorSpec.Builder getKeyPairGeneratorSpecBuilder(String alias, GenerateKeyAttribute attributes) {
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(attributes.getKeySize(), RSAKeyGenParameterSpec.F4);
        return super.getKeyPairGeneratorSpecBuilder(alias, attributes).setAlgorithmParameterSpec(spec);
    }
}
