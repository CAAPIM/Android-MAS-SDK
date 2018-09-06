/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.ScenarioInfo;
import com.ca.mas.ScenarioMasterInfo;
import com.ca.mas.Scenarios;
import com.ca.mas.TestId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASPerformanceTest extends MASMockGatewayTestBase {

    private static final String PROFILER_CONFIG_FILE = "profiler_config.json";
    private  final int TENS = 1000;
    private static final String TAG = MASPerformanceTest.class.getSimpleName();
    private static Context context = null;
    private static Map<Integer, ScenarioInfo> map = new HashMap<>();
    private static boolean isBenchmark = false;
    private static Scenarios scenarios;

    @BeforeClass
    public static void loadConfig() {

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String jsonString = readJSONFromAsset();
        Gson gson = new GsonBuilder().create();

        scenarios = gson.fromJson(jsonString, Scenarios.class);
        ScenarioMasterInfo masterConfig = scenarios.getMaster();

        if(masterConfig.getOperation_type().equalsIgnoreCase("benchmark"))  {
            isBenchmark = true;
        }

        for (ScenarioInfo scenarioInfo : scenarios.getScenarios()){
            map.put(scenarioInfo.getId(), scenarioInfo);
            if(!isBenchmark){
                scenarioInfo.setIteration(1);
            }
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
        MAS.processPendingRequests();
        MAS.stop();

    }

    @AfterClass
    public static void compelete(){
        if(isBenchmark){
            printUpdatedBenchmark();
        }

    }


    @Test
    @TestId(1)
    public void loginFlow() {


        TestId testId = new Object() {}.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
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
            Log.d(TAG, "Duration of login flow for iteration " + i + " = " + (end - start)/(double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration()* TENS);
        if(isBenchmark)  {
            scenarioInfo.setBenchmark(avg);
        }

        Log.d(TAG, "Benchmark = " + avg + "s");

        assertTrue("Taken more than " +scenarioInfo.getBenchmark() +" time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(2)
    public void loginLogoutFlow() {

        TestId testId = new Object() {}.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);

        Double sum = 0.0;

        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();
            MAS.start(getContext());
            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
            MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser result) {
                    result.logout(true, new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onError(Throwable e) {
                            countDownLatch.countDown();
                        }
                    });

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
            Log.d(TAG, "Duration of login - logout flow for iteration " + i + " = " + (end - start)/(double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration()* TENS);
        if(isBenchmark)  {
            scenarioInfo.setBenchmark(avg);
        }

        Log.d(TAG, "Benchmark = " + avg + "s");

        assertTrue("Taken more than " +scenarioInfo.getBenchmark() +" time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(3)
    public void loginDeregisterFlow() {

        TestId testId = new Object() {}.getClass().getEnclosingMethod().getAnnotation(TestId.class);
        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);

        Double sum = 0.0;
        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();
            MAS.start(getContext());
            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
            MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser result) {
                    MASDevice.getCurrentDevice().deregister(new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onError(Throwable e) {
                            countDownLatch.countDown();
                        }
                    });

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
            Log.d(TAG, "Duration of login - deregister flow for iteration " + i + " = " + (end - start)/(double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration()* TENS);
        if(isBenchmark)  {
            scenarioInfo.setBenchmark(avg);
        }

        Log.d(TAG, "Benchmark = " + avg + "s");

        assertTrue("Taken more than " +scenarioInfo.getBenchmark() +" time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(4)
    public void loginGetFlow() throws MalformedURLException {

        TestId testId = new Object() {}.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
        Double sum = 0.0;
        final MASRequest request = new MASRequest.MASRequestBuilder(new URL(
                MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                        GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();

        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();
            MAS.start(getContext());
            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);

            MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser result) {
                    MAS.invoke(request,new MASCallback<MASResponse<JSONObject>>()

                    {

                        @Override
                        public void onSuccess (MASResponse < JSONObject > result) {
                            Log.d(TAG, result.toString());
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onError (Throwable e){
                            countDownLatch.countDown();
                        }
                    });
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
            Log.d(TAG, "Duration of login - get flow for iteration " + i + " = " + (end - start)/(double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration()* TENS);
        if(isBenchmark)  {
            scenarioInfo.setBenchmark(avg);
        }
        Log.d(TAG, "Benchmark = " + avg + "s");
        assertTrue("Taken more than " +scenarioInfo.getBenchmark() +" time to execute", avg <= scenarioInfo.getBenchmark());

    }


    private static void printUpdatedBenchmark() {

        Gson gson = new GsonBuilder().create();
        String jsonStr = gson.toJson(scenarios);
        try {
            Log.d(TAG, " \n\n"+new JSONObject(jsonStr).toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }


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
        return "\n\n"+json;
    }




}
