/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */
package com.ca.mas.foundation;

import com.ca.mas.MASStartTestBase;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class MASConfigurationTest extends MASStartTestBase {
    @Test
    public void testConfiguration() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        Assert.assertEquals("https://localhost:41979", MASConfiguration.getCurrentConfiguration().getGatewayUrl().toString());
        Assert.assertFalse(MASConfiguration.getCurrentConfiguration().isEnabledPublicKeyPinning());
        Assert.assertTrue(MASConfiguration.getCurrentConfiguration().getLocationIsRequired());
        Assert.assertEquals("AppA", MASConfiguration.getCurrentConfiguration().getApplicationName());
        Assert.assertEquals("confidential", MASConfiguration.getCurrentConfiguration().getApplicationType());
        Assert.assertEquals("AutomationTestApp", MASConfiguration.getCurrentConfiguration().getApplicationDescription());
        Assert.assertEquals("CA Technologies", MASConfiguration.getCurrentConfiguration().getApplicationOrganization());
        Assert.assertEquals("admin", MASConfiguration.getCurrentConfiguration().getApplicationRegisteredBy());
        Assert.assertEquals("/auth/oauth/v2/token", MASConfiguration.getCurrentConfiguration().getEndpointPath("msso.url.request_token"));
        Assert.assertNotNull(MASConfiguration.getCurrentConfiguration().getGatewayCertificates());
        Assert.assertFalse(MASConfiguration.getCurrentConfiguration().isEnabledTrustedPublicPKI());

        MASConfiguration.getCurrentConfiguration().setCertificateAdvancedRenewTimeframe(3);
        Assert.assertEquals(3, MASConfiguration.getCurrentConfiguration().getCertificateAdvancedRenewTimeframe());

        Assert.assertTrue(MASConfiguration.getCurrentConfiguration().isLoaded());
        Assert.assertEquals("localhost", MASConfiguration.getCurrentConfiguration().getGatewayHostName());
        Assert.assertEquals(41979, MASConfiguration.getCurrentConfiguration().getGatewayPort());
        Assert.assertEquals("", MASConfiguration.getCurrentConfiguration().getGatewayPrefix());
        Assert.assertTrue(MASConfiguration.getCurrentConfiguration().isSsoEnabled());
    }
}
