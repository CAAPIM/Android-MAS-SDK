/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;

import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.Test;

public class MASOtpTest extends MASIntegrationBaseTest {


    @Test
    public void testOTP() throws Exception {
        /*z

        MAS.setOtpAuthenticationListener(new MASOtpAuthenticationListener {

            void onOtpRequested (OtpRequestHandler handler){

                //Developer may prompt for OTP request, pass the handler to the Fragment

                handler.selectOtpDeliveryChannel(... ,new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

                //Once it get the otp, invoke proceed, else invoke handler.cancel()
                handler.proceed(otp); //This will endup put the otp to API header.

            }

        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/superprotected")).get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
            }

            @Override
            public void onError(Throwable e) {
            }
        });
        */


    }
}
