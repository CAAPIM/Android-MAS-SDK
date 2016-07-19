/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;

@RunWith(AndroidJUnit4.class)
public class InitSDKTest extends AndroidTestCase {

    private String config = "{\n" +
            "  \"server\": {\n" +
            "    \"hostname\": \"test.ca.com\"\n" +
            "  },\n" +
            "  \"oauth\": {\n" +
            "    \"client\": {\n" +
            "      \"organization\": \"CA Technologies\"," +
            "      \"client_ids\": [\n" +
            "        {\n" +
            "          \"client_id\": \"3f27bb4f-b5aa-458b-962b-14d352b7977c\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    public void initSDKWithJSONObject() throws JSONException {
        MobileSso mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), new JSONObject(config));
        URI uri = mobileSso.getURI("myservice");
        assertNotNull(mobileSso);
        assertEquals("https://test.ca.com:8443/myservice", uri.toString());
    }

    @Test
    public void initSDKWithFileIRL() throws Exception {
        File file = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir(), "test.json");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(config);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        MobileSso mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(), file.toURI().toURL());
        URI uri = mobileSso.getURI("myservice");
        assertNotNull(mobileSso);
        assertEquals("https://test.ca.com:8443/myservice", uri.toString());

        file.delete();

    }
}
