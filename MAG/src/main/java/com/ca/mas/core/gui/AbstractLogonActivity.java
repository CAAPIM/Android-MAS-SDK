/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.ca.mas.core.auth.AuthRenderer;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.creds.PasswordCredentials;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.core.service.Provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public abstract class AbstractLogonActivity extends Activity {

    private Map<String, List<AuthRenderer>> renderers = new HashMap<String, List<AuthRenderer>>();
    List<AuthRenderer> rendererInstances = new ArrayList<AuthRenderer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendCancelIntent();
    }

    @Override
    protected void onDestroy() {
        for (AuthRenderer r : rendererInstances) {
            r.close();
        }
        rendererInstances.clear();
        super.onDestroy();
    }

    /**
     * Once the username and password are obtained, use this method to send intent to the Mobile SSO Module to proceed
     * the logon process.
     *
     * @param username The username
     * @param password The password
     */
    protected void sendCredentialsIntent(String username, String password) {
        Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, getBaseContext(), MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, findRequestId());
        intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, new PasswordCredentials(username, password == null ? null : password.toCharArray()));
        startService(intent);

    }

    /**
     * Once the authorization code is obtained, use this method to send intent to the Mobile SSO Module to proceed
     * the logon process.
     *
     * @param credentials The Credential retrieved by the social login platform.
     */
    public void sendCredentialsIntent(Credentials credentials) {
        Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, getBaseContext(), MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, findRequestId());
        intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, credentials);
        startService(intent);
    }

    /**
     * Cancel the logon process.
     */
    protected void sendCancelIntent() {
        Intent intent = new Intent(MssoIntents.ACTION_CANCEL_REQUEST, null, getBaseContext(), MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, findRequestId());
        startService(intent);
    }

    /**
     * Retrieve the request id from the intent that start this activity.
     *
     * @return The request id or -1 if not found.
     */
    private long findRequestId() {
        long id = -1;
        Bundle extras = getExtras();
        if (extras != null) {
            id = extras.getLong(MssoIntents.EXTRA_REQUEST_ID);
        }
        return id;
    }

    /**
     * <p>Returns a list of View for the Authorize Provider. Each Authorize Provider contains logic and action to perform
     * the login process.</p>
     * <ul>
     * <li>For Social Login - A {@link ImageButton} will represent the login view, the
     * {@link ImageButton#setOnClickListener(View.OnClickListener)} will launch
     * a WebView and trigger the social login process. Make sure the social login icons has been stored in the res
     * folder, otherwise it won't include in the returned list.
     * </li>
     * <li>For QRCode - A {@link com.ca.mas.core.auth.QRCode} will represent the login view. Scan the QRCode with
     * another device to perform the login.
     * </li>
     * </ul>
     *
     * @return A List of View which represent the Authorize Provider.
     * @see com.ca.mas.core.gui.AbstractLogonActivity#addAuthRenderer(com.ca.mas.core.auth.AuthRenderer)
     * @see com.ca.mas.core.gui.AbstractLogonActivity#setAuthRenderer(com.ca.mas.core.auth.AuthRenderer)
     */
    protected List<View> getProviders() {
        List<View> views = new ArrayList<View>();
        Bundle extras = getExtras();
        if (extras != null) {
            AuthenticationProvider authProvider = extras.getParcelable(MssoIntents.EXTRA_AUTH_PROVIDERS);
            if (authProvider != null) {
                List<Provider> providers = authProvider.getProviders();
                for (final Provider provider : providers) {
                    List<AuthRenderer> renderer = renderers.get(provider.getId());
                    if (renderer != null) {
                        processRenderers(views, renderer, provider);
                        continue;
                    }
                    if (provider.getIconId() != null && provider.getIconId() != 0) {
                        ImageButton button = new ImageButton(this);
                        button.setContentDescription(provider.getId());
                        button.setBackgroundResource(provider.getIconId());
                        if (authProvider.getIdp().equals(AuthenticationProvider.ALL) || authProvider.getIdp().equalsIgnoreCase(provider.getId())) {
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = AbstractLogonActivity.this.getIntent();
                                    intent.putExtra(MssoIntents.EXTRA_SOCIAL_LOGIN_URL, provider.getUrl());
                                    intent.setClass(getBaseContext(), SocialLoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            button.setClickable(false);
                            button.setEnabled(false);
                        }
                        views.add(button);
                    }
                }
            }
        }
        return views;
    }

    /**
     * Check to see if user has already been login with Enterprise instead of Social Login.
     *
     * @return True if user has already been login with Enterprise, otherwise false.
     */
    protected boolean isEnterpriseLoginEnabled() {
        Bundle extras = getExtras();
        if (extras != null) {
            AuthenticationProvider authProvider = extras.getParcelable(MssoIntents.EXTRA_AUTH_PROVIDERS);
            if (authProvider != null) {
                return authProvider.isEnterpriseEnabled();
            } else {
                return true;
            }
        }
        return false;
    }

    private void processRenderers(List<View> views, List<AuthRenderer> renderers, Provider provider) {
        for (AuthRenderer r : renderers) {
            rendererInstances.add(r);
            if (r.init(AbstractLogonActivity.this, provider)) {
                View view = r.render();
                if (view != null) {
                    views.add(view);
                }
                r.onRenderCompleted();
            }
        }
    }

    /**
     * Replace with the new AuthRenderer.
     *
     * @param authRenderer the new authenticate renderer
     */
    protected void setAuthRenderer(AuthRenderer authRenderer) {

        String id = authRenderer.getId();
        List<AuthRenderer> rs = new ArrayList<AuthRenderer>();
        rs.add(authRenderer);
        renderers.put(id, rs);
    }

    /**
     * Add a new AuthRenderer
     *
     * @param authRenderer  the new authenticate renderer
     */
    protected void addAuthRenderer(AuthRenderer authRenderer) {

        String id = authRenderer.getId();
        List<AuthRenderer> rs = renderers.get(id);
        if (rs == null) {
            setAuthRenderer(authRenderer);
        } else {
            rs.add(authRenderer);
        }
    }

    private Bundle getExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getExtras();
        }
        return null;
    }

}
