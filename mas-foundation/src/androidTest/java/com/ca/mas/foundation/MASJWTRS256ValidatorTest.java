/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.content.SharedPreferences;

import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTRS256Validator;
import com.ca.mas.core.token.JWTValidationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


public class MASJWTRS256ValidatorTest extends MASMockGatewayTestBase {


    @Test(expected = JWTValidationException.class)
    public void validateWithKidMissingTest() throws Exception {
        MAS.start(getContext());
        IdToken idToken = new IdToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjo5OTk5OTk5OTk5LCJhdWQiOiJkdW1teSIsImF6cCI6ImY0NzM1MjVkLWMxMzAtNGJiYS04NmNjLWRiMjZkODg3NTM4NiJ9.Q25Tm1yqs-KLR_qX-t6iuq38K_yFeobil3oMAXx9E2L1ds-DUG6tzm3BNQZUTQdNALRI47pGJUF4ZLJkqyC-z_THqwZwBq9ISfalmDxmSdf_ec7qt6Ll-mFj7epAfMY5JsEG7YO6ReDmfToke95ZJup9x25GHZOuH_gyiSd94SM", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        JWTRS256Validator jwtrs256Validator = new JWTRS256Validator();
        jwtrs256Validator.validate(idToken);
    }

    @Test(expected = JWTValidationException.class)
    public void validateWithNonRS256AlgTest() throws Exception {
        MAS.start(getContext());
        IdToken idToken = new IdToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHRfc3NsX2tleSJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjo5OTk5OTk5OTk5LCJhdWQiOiJkdW1teSIsImF6cCI6ImY0NzM1MjVkLWMxMzAtNGJiYS04NmNjLWRiMjZkODg3NTM4NiJ9.Q25Tm1yqs-KLR_qX-t6iuq38K_yFeobil3oMAXx9E2L1ds-DUG6tzm3BNQZUTQdNALRI47pGJUF4ZLJkqyC-z_THqwZwBq9ISfalmDxmSdf_ec7qt6Ll-mFj7epAfMY5JsEG7YO6ReDmfToke95ZJup9x25GHZOuH_gyiSd94SM", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        JWTRS256Validator jwtrs256Validator = new JWTRS256Validator();
        jwtrs256Validator.validate(idToken);
    }

    @Test(expected = JWTValidationException.class)
    public void validateWithInvalidKidTest() throws Exception {
        MAS.start(getContext());
        IdToken idToken = new IdToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHRfa2V5In0=.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjo5OTk5OTk5OTk5LCJhdWQiOiJkdW1teSIsImF6cCI6ImY0NzM1MjVkLWMxMzAtNGJiYS04NmNjLWRiMjZkODg3NTM4NiJ9.Q25Tm1yqs-KLR_qX-t6iuq38K_yFeobil3oMAXx9E2L1ds-DUG6tzm3BNQZUTQdNALRI47pGJUF4ZLJkqyC-z_THqwZwBq9ISfalmDxmSdf_ec7qt6Ll-mFj7epAfMY5JsEG7YO6ReDmfToke95ZJup9x25GHZOuH_gyiSd94SM", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        JWTRS256Validator jwtrs256Validator = new JWTRS256Validator();
        jwtrs256Validator.validate(idToken);
    }

    @Test
    public void validSignatureTest() throws Exception {
        MAS.start(getContext());
        IdToken idToken = new IdToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImRlZmF1bHRfc3NsX2tleSJ9.ewogInN1YiI6ICJlaHAxdzVrN3pVdFRrRWYyWEUtSUxuREFhU3FsRHZrY1VXSjZTTTNEUFNzIiwKICJhdWQiOiAiMDdmNjFlNmUtZTE2NC00MTRlLTkyNTItMzMzOTgwOTBmOTAwIiwKICJhY3IiOiAiMCIsCiAiYXpwIjogIllVdEpkR2haZW1kcldteFdkazFpYVRsMU5rTTJkazFYYTNsdlBRPT0iLAogImF1dGhfdGltZSI6IDE1MzkwNzU0OTgsCiAiaXNzIjogImh0dHBzOi8vbWFnZmlkby5jYS5jb206ODQ0MyIsCiAiZXhwIjogMTUzOTE2MTg5OCwKICJpYXQiOiAxNTM5MDc1NDk4Cn0.bW-mEvwxS8AgGWjtpN_DhxobVusW3212m54iK030zcyhBQLUK2M_HJFjVBZByZoMqNLp9zflD8lbxZiOI_JOS3O2ca60gLyvWdEDsZO5dCyMeHZb_6T4FjXRn3v2BktF7raJjRWDfIZRcRZKSLXezig1HFSSYasXlkHyP1vwqs-r_c2oYVmmwQubdcp-PObhqxjO8PGRCB58eYX6lkMXj3AAnl1jeuRmHUns24WjgSWBXQFhSxwJOiXfTajh-bzbjnpeWsKEgVI7BGuIvdIm_YagVo8l3K68dHtuhqTwqiOv9K_z4GlJezdzj4ipx3Wkjm_RjayIWZSLz3-A3AvzWA", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        JWTRS256Validator jwtrs256Validator = new JWTRS256Validator();
        Assert.assertTrue(jwtrs256Validator.validate(idToken));
    }



}
