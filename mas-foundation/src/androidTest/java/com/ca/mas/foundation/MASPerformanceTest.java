/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.ScenarioInfo;
import com.ca.mas.ScenarioMasterInfo;
import com.ca.mas.Scenarios;
import com.ca.mas.TestId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASPerformanceTest extends MASMockGatewayTestBase {

    private static final String PROFILER_CONFIG_FILE = "profiler_config.json";
    private final String TAG = MASPerformanceTest.class.getSimpleName();
    private static Context context = null;
    private static Map<Integer, ScenarioInfo> map = new HashMap<>();

    @BeforeClass
    public static void loadConfig() {

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String jsonString = readJSONFromAsset();
        Gson gson = new GsonBuilder().create();
        Scenarios scenarios = gson.fromJson(jsonString, Scenarios.class);
        ScenarioMasterInfo masterConfig = scenarios.getMaster();
        for (ScenarioInfo scenarioInfo : scenarios.getScenarios()){
            map.put(scenarioInfo.getId(), scenarioInfo);
        }


    }

    @Before
    public void init() {
        MAS.start(context);
        if (MASUser.getCurrentUser() != null) {
            MASUser.getCurrentUser().logout(true, null);
        }
        if (MASDevice.getCurrentDevice() != null && MASDevice.getCurrentDevice().isRegistered()) {
            MASDevice.getCurrentDevice().deregister(null);
        }
        MAS.stop();
    }


    @Test
    @TestId(1)
    public void loginFlow() {


        TestId testId = new Object() {}.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int value = testId.value();
        ScenarioInfo scenarioInfo = map.get(value);

        Double sum = 0.0;
        MAS.start(getContext());

        MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);

        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();
            MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser result) {
                    countDownLatch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    countDownLatch.countDown();

                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long end = System.currentTimeMillis();

            sum = sum + (end - start);
            Log.d(TAG, "Duration of login flow for iteration " + i + " = " + (end - start));
        }

        Double avg = sum / scenarioInfo.getIteration();
        Log.d(TAG, "Benchmark = " + avg + "ms");
        assertTrue("Taken more than " +scenarioInfo.getBenchmark() +" time to execute", avg <= scenarioInfo.getBenchmark());

    }


    public static String readJSONFromAsset() {
        String json = null;
        try {

            InputStream is = context.getAssets().open(PROFILER_CONFIG_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}