/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import androidx.test.runner.AndroidJUnit4;

import com.ca.mas.MASMockGatewayTestBase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MASStartTest extends MASMockGatewayTestBase {

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
            "  },\n" +
            "  \"mag\": {\n" +
            "    \"mobile_sdk\": {\n" +
            "      \"trusted_public_pki\": true\n" +
            "    }\n" +
            "  }" +
            "}";

    @Test
    public void startWithFileIRL() {
        File file = new File(getContext().getFilesDir(), "test.json");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(config);
            outputStreamWriter.close();

            MAS.start(getContext(), file.toURI().toURL());
        } catch (Exception e) {
            fail();
        } finally {
            file.delete();
        }

        Assert.assertEquals("test.ca.com", MASConfiguration.getCurrentConfiguration().getGatewayHostName());
    }

}
