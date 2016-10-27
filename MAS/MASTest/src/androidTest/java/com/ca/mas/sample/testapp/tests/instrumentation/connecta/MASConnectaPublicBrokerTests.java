package com.ca.mas.sample.testapp.tests.instrumentation.connecta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;
import com.ca.mas.messaging.util.MessagingConsts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MASConnectaPublicBrokerTests {

    private static final String TAG = MASConnectaPublicBrokerTests.class.getSimpleName();

    @Before
    public void initConnecta() {
        MAS.start(InstrumentationRegistry.getInstrumentation().getTargetContext(), true);
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            final Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASConnectaManager.getInstance().start(ctx);

            MASConnectOptions connectOptions = new MASConnectOptions();
            connectOptions.setServerURIs(new String[]{"tcp://mosquitto.org:1883"});

            MASConnectaManager masConnectaManager = MASConnectaManager.getInstance();
            masConnectaManager.setConnectOptions(connectOptions);
            masConnectaManager.setClientId("clientIadf78asdf8dsf8sda7f8sdaf87s8f7dd");
            masConnectaManager.connect(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in Connecta.connect " + e;
                    latch.countDown();
                }
            });

            latch.await();
            if (!(boolean) result[0]) {
                Log.w(TAG, "initConnecta onError: " + result[1]);
                fail("Reason: " + result[1]);
            }

        } catch (Throwable t) {
            Log.w(TAG, "initConnecta error: ", t);
            fail("" + t);
        }
    }

    @Test
    public void testSubscribe(){
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};

            //setup topic
            MASTopic topic = new MASTopicBuilder().setCustomTopic("test").enforceTopicStructure(false).build();

            // Subscribe to a given topic
            MASConnectaManager.getInstance().subscribe(topic, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "" + e.getMessage();
                    fail("" + e);
                    latch.countDown();

                }
            });

            latch.await();
            if (!(boolean) result[0]) {
                Log.w(TAG, "testAlignPubSub onError: " + result[1]);
                fail("Reason: " + result[1]);
            }
        } catch (InterruptedException e) {
            fail("" + e);
        }
    }

    @Test
    public void testPublish(){
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};

            //setup topic
            MASTopic topic = new MASTopicBuilder().setCustomTopic("test").enforceTopicStructure(false).build();

            //setup message
            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
            masMessage.setPayload("Android Studio test message".getBytes());

            // Send a message to a given topic
            MASConnectaManager.getInstance().publish(topic, masMessage, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "" + e.getMessage();
                    fail("" + e);
                    latch.countDown();
                }
            });

            latch.await();
            if (!(boolean) result[0]) {
                Log.w(TAG, "testAlignPubSub onError: " + result[1]);
                fail("Reason: " + result[1]);
            }
        } catch (InterruptedException e) {
            fail("" + e);
        }
    }

}
