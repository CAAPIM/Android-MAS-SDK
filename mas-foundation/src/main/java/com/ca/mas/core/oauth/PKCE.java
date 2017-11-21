/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */


package com.ca.mas.core.oauth;

/**
 * Created by mujmo02 on 22/10/17.
 */

public class PKCE {
    public String codeChallenge;
    public String codeChallengeMethod;
    public String codeVerifier;

    public PKCE(String codeChallengeMethod, String codeChallenge, String codeVerifier) {
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.codeVerifier = codeVerifier;
    }
}