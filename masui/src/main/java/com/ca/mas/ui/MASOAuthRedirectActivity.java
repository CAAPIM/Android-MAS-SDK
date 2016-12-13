/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ca.mas.foundation.MASAuthorizationResponse;

public class MASOAuthRedirectActivity extends AppCompatActivity {
    private static final String USED_INTENT = "USED_INTENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        Uri redirectUri = getIntent().getData();
        if (redirectUri != null) {
            MASAuthorizationResponse response = MASAuthorizationResponse.fromUri(redirectUri);
            //MASUser.getCurrentUser().login(response);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        checkIntent(getIntent());
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        checkIntent(intent);
//    }
//
//    private void checkIntent(@Nullable Intent intent) {
//        if (intent != null) {
//            String action = intent.getAction();
//            switch (action) {
//                case "com.masui.oauth.HANDLE_AUTHORIZATION_RESPONSE":
//                    if (!intent.hasExtra(USED_INTENT)) {
//                        handleAuthorizationResponse(intent);
//                        intent.putExtra(USED_INTENT, true);
//                    }
//                    break;
//                default:
//                    // do nothing
//            }
//        }
//    }
//
//    /**
//     * Exchanges the code, for the {@link TokenResponse}.
//     *
//     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
//     */
//    private void handleAuthorizationResponse(@NonNull Intent intent) {
//        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
//        AuthorizationException error = AuthorizationException.fromIntent(intent);
//        final AuthState authState = new AuthState(response, error);
//
//        if (response != null) {
//            Log.i(TAG, String.format("Handled Authorization Response %s ", authState.toString()));
//            AuthorizationService service = new AuthorizationService(this);
//            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
//                @Override
//                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
//                    if (exception != null) {
//                        Log.w(TAG, "Token Exchange failed", exception);
//                    } else {
//                        if (tokenResponse != null) {
//                            authState.update(tokenResponse, exception);
//                            persistAuthState(authState);
//                            Log.i(TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
//                        }
//                    }
//                }
//            });
//        }
//    }
}
