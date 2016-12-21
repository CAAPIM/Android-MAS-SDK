package com.ca.mas.sample.testapp.tests.instrumentation.foundation;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASUnProtectedAPITest {

    @Test
    public void testInvokeUnprotected() throws Exception {
        MAS.debug();
        MAS.start(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);

        Uri.Builder builder = new Uri.Builder();
        //An endpoint in the server which required SSL only
        builder.appendEncodedPath("auth/device/authorization/test");
        builder.appendQueryParameter("operation", "listProducts");
        MASRequest request = new MASRequest.MASRequestBuilder(builder.build())
                .unProtected()
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                if (HttpURLConnection.HTTP_OK == response.getResponseCode()) {
                    JSONObject j = response.getBody().getContent();
                    result[0] = true;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
        //latch.await();

        assertTrue(result[0]);

    }
}
