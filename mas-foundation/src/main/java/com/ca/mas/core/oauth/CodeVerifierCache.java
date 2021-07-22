/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import com.ca.mas.core.storage.sharedstorage.SharedPreferencesUtil;

/**
 * Temporary cache to store code verifier
 */
public class CodeVerifierCache {

    private static CodeVerifierCache instance = new CodeVerifierCache();
    private SharedPreferencesUtil prefUtil = null;

    private CodeVerifierCache() {
        prefUtil = new SharedPreferencesUtil("codeverifier");
    }

    public static CodeVerifierCache getInstance() {
        return instance;
    }

    public void store(String state, String codeVerifier) {
        prefUtil.save("state", state);
        prefUtil.save("code", codeVerifier);
    }

    public String take(String state) {
        if (prefUtil.getString("state") == null && state != null
                || prefUtil.getString("state")!= null && !prefUtil.getString("state").equals(state)) {
            throw new IllegalStateException("OAuth State Mismatch");
        }
        String cv = prefUtil.getString("code");
        prefUtil.delete("state");
        prefUtil.delete("code");
        return cv;
    }

    //Workaround for pre MAG 3.3, Defect reference DE256594
    public String take() {
        String cv = prefUtil.getString("code");
        prefUtil.delete("state");
        prefUtil.delete("code");
        return cv;
    }


}
