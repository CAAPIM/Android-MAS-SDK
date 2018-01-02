/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

/**
 * Attributes to generate keys
 */
public class GenerateKeyAttribute {

    private int keySize = 2048;
    private boolean userAuthenticationRequired = false;
    private boolean encryptionRequired = false;
    private int userAuthenticationValidityDurationSeconds = -1;
    private boolean invalidatedByBiometricEnrollment = false;
    private String dn = "";

    public boolean isEncryptionRequired() {
        return encryptionRequired;
    }

    public void setEncryptionRequired(boolean encryptionRequired) {
        this.encryptionRequired = encryptionRequired;
    }

    public int getKeySize() {
        return keySize;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public boolean isUserAuthenticationRequired() {
        return userAuthenticationRequired;
    }

    public void setUserAuthenticationRequired(boolean userAuthenticationRequired) {
        this.userAuthenticationRequired = userAuthenticationRequired;
    }

    public int getUserAuthenticationValidityDurationSeconds() {
        return userAuthenticationValidityDurationSeconds;
    }

    public void setUserAuthenticationValidityDurationSeconds(int userAuthenticationValidityDurationSeconds) {
        this.userAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
    }

    public boolean isInvalidatedByBiometricEnrollment() {
        return invalidatedByBiometricEnrollment;
    }

    public void setInvalidatedByBiometricEnrollment(boolean invalidatedByBiometricEnrollment) {
        this.invalidatedByBiometricEnrollment = invalidatedByBiometricEnrollment;
    }

}
