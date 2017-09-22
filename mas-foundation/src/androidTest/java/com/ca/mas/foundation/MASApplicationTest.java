/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.content.ActivityNotFoundException;
import android.widget.ImageView;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.foundation.auth.MASApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASApplicationTest extends MASLoginTestBase {

    @Test
    public void testRetrieveEnterpriseApps() throws ExecutionException, InterruptedException, JSONException {
        MASCallbackFuture<List<MASApplication>> callbackFuture = new MASCallbackFuture<>();
        MASApplication.retrieveEnterpriseApps(callbackFuture);
        List<MASApplication> applications = callbackFuture.get();
        assertEquals(3, applications.size());
        for (MASApplication application: applications) {
            assertNotNull(application.getIdentifier());
            assertNotNull(application.getName());
            assertNotNull(application.getIconUrl());
            assertNotNull(application.getNativeUri());
            assertNotNull(application.getCustom());
        }

        //Test Web App
        MASApplication appC = applications.get(2);
        JSONObject custom = appC.getCustom();
        JSONArray employees = custom.getJSONArray("employees");
        assertEquals(3, employees.length());

        ImageView imageView = new ImageView(getContext());
        appC.renderEnterpriseIcon(imageView);
        final boolean[] clicked = {false};
        MASApplication.MASApplicationLauncher launcher = new MASApplication.MASApplicationLauncher() {
            @Override
            public void onWebAppLaunch(MASApplication application) {
                clicked[0] = true;
            }
        };

        MASApplication.setApplicationLauncher(launcher);
        imageView.callOnClick();
        assertTrue(clicked[0]);

        //Test Native App
        MASApplication appA = applications.get(0);
        appA.renderEnterpriseIcon(imageView);
        try {
            imageView.callOnClick();
            fail();
        } catch (ActivityNotFoundException ignored) {
        }
    }
}
