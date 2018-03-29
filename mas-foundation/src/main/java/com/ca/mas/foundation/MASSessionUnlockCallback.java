/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.annotation.TargetApi;

public interface MASSessionUnlockCallback<T> extends MASCallback<T> {

    /**
     * Called when an asynchronous call encounters a UserNotAuthenticatedException and requires
     * user authentication. This should be implemented from your Activity/Fragment of choice,
     * and launch the default Android Keyguard Service intent:
     *
     * =============================================================================================
     * private final static int FINGERPRINT_REQUEST_CODE = 0x1000;
     *
     * KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
     * Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("", "Please provide your credentials.");
     * if (intent != null) {
     *     startActivityForResult(intent, FINGERPRINT_REQUEST_CODE);
     * }
     * =============================================================================================
     *
     * In addition, you need to implement onActivityResult() in the calling Activity/Fragment
     * to handle the response.
     *
     * =============================================================================================
     * super.onActivityResult(requestCode, resultCode, data);
     * if (requestCode == FINGERPRINT_REQUEST_CODE) {
     *     if (resultCode == RESULT_OK) {
     *         MASUser.getCurrentUser().unlockSession(getUnlockCallback());
     *     } else if (resultCode == RESULT_CANCELED) {
     *         // Handle the canceled case as desired.
     *     }
     * }
     * =============================================================================================
     */
    @TargetApi(23)
    void onUserAuthenticationRequired();

}
