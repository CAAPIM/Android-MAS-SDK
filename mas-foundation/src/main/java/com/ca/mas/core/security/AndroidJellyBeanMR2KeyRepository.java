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

import com.ca.mas.foundation.MAS;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidJellyBeanMR2KeyRepository extends AndroidKeyStoreRepository {

    @Override
    AlgorithmParameterSpec getAlgorithmParameterSpec(String alias, GenerateKeyAttribute attribute) throws KeyStoreException {
        return getKeyPairGeneratorSpecBuilder(alias, attribute).build();
   }

    KeyPairGeneratorSpec.Builder getKeyPairGeneratorSpecBuilder(String alias, GenerateKeyAttribute attributes) {
        // For Android Pre-M (Lollipop and prior)
        // use the KeyPairGeneratorSpec.Builder, deprecated as of Marshmallow
        //    generates the key inside the AndroidKeyStore

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(MAS.getContext())
                .setAlias(alias)
                .setStartDate(now).setEndDate(end)
                .setSerialNumber(BigInteger.valueOf(1))
                .setSubject(new X500Principal(attributes.getDn()));

        if (attributes.isEncryptionRequired()) {
            builder.setEncryptionRequired();
        }

        return builder;

    }
}
