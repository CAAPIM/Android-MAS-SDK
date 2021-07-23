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
 *  Shared preference implementation to store code verifier
 */
public class CodeVerifierCache {

    private static CodeVerifierCache instance = new CodeVerifierCache();
    private SharedPreferencesUtil prefUtil = null;
    private String mState = "##default-state##";

    private CodeVerifierCache() {
        prefUtil = new SharedPreferencesUtil("codeverifier");
    }

    public static CodeVerifierCache getInstance() {
        return instance;
    }

    public void store(String state, String codeVerifier) {
        prefUtil.save(mState, codeVerifier);
        if (state != null) {
            prefUtil.save(state, codeVerifier);
        }
    }

    //Workaround for pre MAG 3.3, Defect reference DE256594
    public String take() {
        String cv = prefUtil.getString(mState);
        if (cv == null) {
            throw new IllegalStateException("OAuth State Mismatch");
        }
        prefUtil.delete(mState);
        return cv;
    }

    public String take(String state) {
        if (state == null) {
            return take();
        }
        String cv = prefUtil.getString(state);
        if (cv == null) {
            throw new IllegalStateException("OAuth State Mismatch");
        }
        prefUtil.delete(state);
        return cv;
    }
}
