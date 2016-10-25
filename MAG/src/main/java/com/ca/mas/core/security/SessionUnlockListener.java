/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

import android.content.Context;

/**
 * This listener should be implemented by the Activity or
 * Fragment in charge of launching the KEYGUARD_SERVICE intent.
 * Sample implementation as follows:
 * <p>
 * <pre>
 * {@code
 * @Override
 * public void triggerDeviceUnlock(Context context) {
 *     private final static int FINGERPRINT_REQUEST_CODE = 1000;
 *
 *     KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
 *     Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
 *     "Unlock Device", "Please provide your credentials.");
 *     if (intent != null) {
 *         startActivityForResult(intent, FINGERPRINT_REQUEST_CODE);
 *     }
 * }
 *
 * @Override
 * public void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     super.onActivityResult(requestCode, resultCode, data);
 *     if (requestCode == FINGERPRINT_REQUEST_CODE) {
 *         if (resultCode == RESULT_OK) {
 *             MASUser.getCurrentUser().unlockSession(this, getUnlockCallback());
 *         } else {
 *             // Handle the cancelling case accordingly
 *         }
 *     }
 * }
 * }</pre>
 */
public interface SessionUnlockListener {
    void triggerDeviceUnlock(Context context);
}
