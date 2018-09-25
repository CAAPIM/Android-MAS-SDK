/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.ScenarioInfo;
import com.ca.mas.ScenarioMasterInfo;
import com.ca.mas.ScenarioTestResult;
import com.ca.mas.Scenarios;
import com.ca.mas.TestId;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASPerformanceTest extends MASMockGatewayTestBase {

    private static final String PROFILER_CONFIG_FILE = "profiler_config.json";
    private final int TENS = 1000;
    private static final String TAG = MASPerformanceTest.class.getSimpleName();
    private static Context context = null;
    private static Map<Integer, ScenarioInfo> map = new HashMap<>();
    private static boolean isBenchmark = false;
    private static Scenarios scenarios;
    private static List<ScenarioTestResult> testResult = new ArrayList<>();

    @BeforeClass
    public static void loadConfig() {

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String jsonString = readJSONFromAsset();
        Gson gson = new GsonBuilder().create();

        scenarios = gson.fromJson(jsonString, Scenarios.class);
        ScenarioMasterInfo masterConfig = scenarios.getMaster();

        if (masterConfig.getOperation_type().equalsIgnoreCase("benchmark")) {
            isBenchmark = true;
        }

        for (ScenarioInfo scenarioInfo : scenarios.getScenarios()) {
            map.put(scenarioInfo.getId(), scenarioInfo);
            if (!isBenchmark) {
                scenarioInfo.setIteration(1);
            } else {
                if (masterConfig.isUse_default())
                    scenarioInfo.setIteration(masterConfig.getIteration());
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
    public static void complete() {
        if (isBenchmark) {
            printUpdatedBenchmark();
        } else {
            printResult();
        }

    }


    @Test
    @TestId(1)
    public void startTest() {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
        Double sum = 0.0;

        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            long start = System.currentTimeMillis();
            MAS.start(getContext());
            long end = System.currentTimeMillis();

            sum = sum + (end - start);
            Log.d(TAG, "Duration of start flow iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }
        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(2)
    public void loginTest() {


        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

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
            Log.d(TAG, "Duration of login flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {

            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }

        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(3)
    public void loginLogoutFlowTest() {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

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
            Log.d(TAG, "Duration of login - logout flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }

        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(4)
    public void loginDeregisterFlowTest() {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);
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
            Log.d(TAG, "Duration of login - deregister flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }

        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(5)
    public void getTest() throws MalformedURLException, ExecutionException, InterruptedException {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
        Double sum = 0.0;
        final MASRequest request = new MASRequest.MASRequestBuilder(new URL(
                MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                        GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();

        MAS.start(getContext());
        MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        assertNotNull(callback.get());

        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();


            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    Log.d(TAG, result.getResponseMessage());
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
            Log.d(TAG, "Duration of get flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }
        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }


    @Test
    @TestId(6)
    public void loginGetFlowTest() throws MalformedURLException {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

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
                    MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                        @Override
                        public void onSuccess(MASResponse<JSONObject> result) {
                            Log.d(TAG, result.toString());
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
            Log.d(TAG, "Duration of login - get flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }
        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }

    @Test
    @TestId(7)
    public void getViaOtpFlowTest() throws URISyntaxException, ExecutionException, InterruptedException {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
        Double sum = 0.0;
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();

        MAS.start(getContext());
        MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);

        callback.get();
        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            long start = System.currentTimeMillis();


            MAS.setAuthenticationListener(new MASAuthenticationListener() {

                @Override
                public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

                }

                @Override
                public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                    handler.deliver("EMAIL", new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            handler.proceed(getContext(), "1234");
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
                }
            });

            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    Log.d(TAG, result.getResponseMessage());
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
            Log.d(TAG, "Duration of get flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }
        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }


    @Test
    @TestId(8)
    public void implicitLoginGetViaOtpFlowTest() throws URISyntaxException {

        TestId testId = new Object() {
        }.getClass().getEnclosingMethod().getAnnotation(TestId.class);

        int id = testId.value();
        ScenarioInfo scenarioInfo = map.get(id);
        Double sum = 0.0;
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();


        for (int i = 0; i < scenarioInfo.getIteration(); i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(2);
            long start = System.currentTimeMillis();

            MAS.start(getContext());
            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
            MAS.setAuthenticationListener(new MASAuthenticationListener() {

                @Override
                public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {


                    MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {
                        @Override
                        public void onSuccess(MASUser result) {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
                }

                @Override
                public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                    handler.deliver("EMAIL", new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                            handler.proceed(getContext(), "1234");
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onError(Throwable e) {
                            countDownLatch.countDown();

                        }
                    });
                }
            });
            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    Log.d(TAG, result.getResponseMessage());
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
            Log.d(TAG, "Duration of get flow for iteration " + i + " = " + (end - start) / (double) TENS + "s");
        }
        double avg = sum / (scenarioInfo.getIteration() * TENS);
        if (isBenchmark) {
            scenarioInfo.setBenchmark(avg);
            Log.d(TAG, "Benchmark for " + scenarioInfo.getName() + "= " + avg + "s");
        } else {
            ScenarioTestResult result = getScenarioTestResult(scenarioInfo, avg);
            testResult.add(result);
            Log.d(TAG, "Execution time for " + scenarioInfo.getName() + "= " + avg + "s");
        }
        assertTrue("Taken more than " + scenarioInfo.getBenchmark() + " time to execute", avg <= scenarioInfo.getBenchmark());

    }


    private static void printUpdatedBenchmark() {

        Gson gson = new GsonBuilder().create();
        String jsonStr = gson.toJson(scenarios);
        try {
            Log.d(TAG, " \n\n" + new JSONObject(jsonStr).toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private static void printResult() {

        Gson gson = new GsonBuilder().create();
        String jsonStr = gson.toJson(testResult);
        try {
            Log.d(TAG, " \n\nTest Results: " + new JSONArray(jsonStr).toString(4));
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
        return "\n\n" + json;
    }

    private ScenarioTestResult getScenarioTestResult(ScenarioInfo scenarioInfo, double avg) {
        ScenarioTestResult result = new ScenarioTestResult();
        result.setBenchmark(scenarioInfo.getBenchmark());
        result.setTestId(scenarioInfo.getId());
        result.setTestName(scenarioInfo.getName());
        result.setCurrentRunTime(avg);
        result.setResult(getResult(scenarioInfo.getBenchmark(), avg));
        return result;
    }

    private String getResult(Double benchmark, double avg) {
        long result = (long) ((benchmark - avg) * 100 / benchmark);
        if (result > 0)
            return result + "% Faster";
        else if (result < 0) {
            return Math.abs(result) + "% Slower";
        } else {
            return "No change";
        }
    }

}
