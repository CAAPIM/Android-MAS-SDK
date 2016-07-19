/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.sample.testapp.tests.instrumentation.connecta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;
import com.ca.mas.messaging.util.MessagingConsts;
import com.ca.mas.sample.testapp.MainActivity;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class MASConnectaTests extends MASIntegrationBaseTest {
    private static final String TAG = MASConnectaTests.class.getSimpleName();
    private static String TOPIC_NAME = "conectaTestTopic";

    /**
     * Initializes Connecta for all messaging related stuffs.
     */
    @Before
    public void initConnecta() {

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            final Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASConnectaManager.getInstance().start(ctx);
            MASConnectaManager.getInstance().connect(new MASCallback<Void>() {
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
    public void testSubscribeSelfTopic() {

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            MASUser usr = MASUser.getCurrentUser();
            try {
                final MASTopic masTopic = new MASTopicBuilder().setUserId(usr.getId()).setCustomTopic(usr.getId()).build();

                MASUser.getCurrentUser().startListeningToTopic(masTopic, new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void object) {
                        result[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        result[0] = false;
                        result[1] = "Cant subscribe to  self topic ";
                        latch.countDown();
                    }
                });
            } catch (MASException e) {
                Log.w(TAG, "testSubscribeSelfTopic exception subscribing to topic: ", e);
                result[0] = false;
                result[1] = "Cant create topic";
                latch.countDown();
            }
            latch.await();
            if (!(boolean) result[0]) {
                Log.w(TAG, "testSubscribeSelfTopic onError: " + result[1]);
                fail("Reason: " + result[1]);
            }
        } catch (Throwable t) {
            Log.w(TAG, "testSubscribeSelfTopic unable to get current user: ", t);
            fail("" + t);
        }
    }

    @Test
    public void testSendValidMessageUser() {

        try {
            // set up the listeners
            BroadcastReceiver receiver = initMessageReceiver();
            MASCallback callback = getMASCallback();
            MASTopic masTopic = configureTopicListeners(callback);

            // get the current MASUser and create a user topic listener
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = new MASTopicBuilder().setUserId(user.getId()).setCustomTopic(TOPIC_NAME).build();

            //setup message
            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
            masMessage.setPayload("TEST Sample Message 1".getBytes());

            sendMessageToTopic(topic, masMessage, false);

            // clean up the listeners
            //mActivityRule.getActivity().unregisterReceiver(receiver);
            //MASUser.getCurrentUser(mActivityRule.getActivity()).stopListeningToTopic(masTopic, callback);

        } catch (Exception e) {
            Log.w(TAG, "testSendValidMessageUser EXCEPTION: ", e);
            fail("" + e);
        }
    }

    @Test
    public void testSendNullMessage() {

        try {
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = new MASTopicBuilder().setUserId(user.getId()).setCustomTopic(TOPIC_NAME).build();

            //setup message
            MASMessage masMessage = null;

            sendMessageToTopic(topic, masMessage, true);
            fail("Expected a Null Exception ");
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendEmptyMessage() {

        try {
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = new MASTopicBuilder().setUserId(user.getId()).setCustomTopic(TOPIC_NAME).build();

            //setup message
            MASMessage masMessage = MASMessage.newInstance();

            sendMessageToTopic(topic, masMessage, false);
        } catch (Exception e) {
            fail("" + e);
        }
    }

    @Ignore
    @Test
    public void testSendInValidMessage() {

        try {
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = new MASTopicBuilder().setUserId(user.getId()).setCustomTopic(TOPIC_NAME).build();

            //setup message
            MASMessage masMessage;
            masMessage = MASMessage.newInstance();
            masMessage.setContentType("type/x.media");
            masMessage.setPayload("test".getBytes());
            masMessage.setTopic(null);

            sendMessageToTopic(topic, masMessage, true);
            fail("Expected Failure");
        } catch (Exception e) {
            fail("" + e);
        }
    }

    @Test
    public void testSendMessageToNullTopic() {

        try {
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = null;

            //setup message
            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
            masMessage.setPayload("test".getBytes());

            sendMessageToTopic(topic, masMessage, true);
            fail("Expected a Null Exception ");
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Ignore
    @Test
    public void testSendMessageToInvalidTopic() {

        try {
            MASUser user = MASUser.getCurrentUser();

            //setup topic
            MASTopic topic = new MASTopicBuilder().setUserId(user.getId()).setCustomTopic("nonexistent_topic").build();

            //setup message
            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
            masMessage.setPayload("test".getBytes());

            sendMessageToTopic(topic, masMessage, true);
        } catch (Exception e) {
            fail("" + e);
        }
    }

    /**
     * Utility class that sends out a message.
     *
     * @param topic
     * @param masMessage
     * @param shouldFail
     */
    private void sendMessageToTopic(MASTopic topic, MASMessage masMessage, boolean shouldFail) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            MASUser user = MASUser.getCurrentUser();

            user.sendMessage(topic, masMessage, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "" + e;
                    latch.countDown();
                }
            });

            latch.await();
            if (shouldFail) {
                if ((boolean) result[0]) {
                    Log.w(TAG, "sendMessageToTopic should have failed but didn't!");
                    fail("Expected failure ");
                }
            } else {
                if (!(boolean) result[0]) {
                    Log.w(TAG, "sendMessageToTopic failed (but shouldn't), " + result[1]);
                    fail("Reason: " + result[1]);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "sendMessageToTopic EXCEPTION: ", t);
            throw t;
        }
    }

    @Test
    public void testSendMessageToNullUser() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, ""};

            sender.getUserById("asdf", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser user) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[1] = e.toString();
                    latch.countDown();
                }
            });

            await(latch);
            boolean testResult = (boolean) result[0];
            assertEquals(testResult, false);
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendNullMessageToValidUser() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final MASMessage masMessage = null;

            sender.getUserById("spock", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    try {
                        sendMessageToUser(masMessage, result, true);
                        fail("Expected a Null Exception ");
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    fail("Expected a valid user.");
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendEmptyMessageToValidUser() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final MASMessage masMessage = MASMessage.newInstance();

            sender.getUserById("spock", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    try {
                        sendMessageToUser(masMessage, result, false);
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    fail("Expected a valid user.");
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendValidMessageToValidUser() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final MASMessage masMessage = MASMessage.newInstance();
            masMessage.setTopic("testTopic");

            sender.getUserById("spock", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    try {
                        sendMessageToUser(masMessage, result, false);
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    fail("Expected a valid user.");
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendMessageToValidUserWithNullTopic() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final MASMessage message = MASMessage.newInstance();
            final String topic = null;

            sender.getUserById("spock", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    try {
                        sendMessageToUserWithTopic(message, result, topic, false);
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    fail("Expected a valid user.");
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    @Test
    public void testSendMessageToValidUserWithTopic() throws Exception {
        try {
            final MASUser sender = MASUser.getCurrentUser();
            final MASMessage message = MASMessage.newInstance();
            final String topic = "test";

            sender.getUserById("spock", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    try {
                        sendMessageToUserWithTopic(message, result, topic, false);
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    fail("Expected a valid user.");
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Exception: " + e);
        }
    }

    private void sendMessageToUser(MASMessage masMessage, MASUser otherUser, boolean shouldFail) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, ""};
            MASUser user = MASUser.getCurrentUser();

            user.sendMessage(masMessage, otherUser, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[1] = e.toString();
                    latch.countDown();
                }
            });

            await(latch);
            if (shouldFail && (boolean) result[0]) {
                Log.w(TAG, "sendMessageToUser should have failed but didn't.");
                fail("Expected sendMessageToUser to fail.");
            } else if (!(boolean) result[0]) {
                Log.w(TAG, "sendMessageToUser failed (but shouldn't): " + result[1]);
                fail("Reason: " + result[1]);
            }

            assertEquals(result[0], true);
        } catch (Throwable t) {
            Log.w(TAG, "Exception: ", t);
            throw t;
        }
    }

    private void sendMessageToUserWithTopic(MASMessage masMessage, MASUser otherUser, String topic, boolean shouldFail) throws Exception {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            MASUser user = MASUser.getCurrentUser();

            user.sendMessage(masMessage, otherUser, topic, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[1] = e.toString();
                    latch.countDown();
                }
            });

            await(latch);
            if (shouldFail && (boolean) result[0]) {
                Log.w(TAG, "sendMessageToUser should have failed but didn't.");
                fail("Expected sendMessageToUser to fail.");
            } else if (!(boolean) result[0]) {
                Log.w(TAG, "sendMessageToUser failed (but shouldn't): " + result[1]);
                fail("Reason: " + result[1]);
            }

            assertEquals(result[0], true);
        } catch (Throwable t) {
            Log.w(TAG, "sendMessageToTopic EXCEPTION: ", t);
            throw t;
        }
    }

    @Test
    public void testStartListeningToMyMessages() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, ""};

            MASUser.getCurrentUser().startListeningToMyMessages(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[1] = "Cant subscribe to self topic ";
                    latch.countDown();
                }
            });

            await(latch);
            if (!(boolean) result[0]) {
                Log.w(TAG, "testSubscribeSelfTopic onError: " + result[1]);
                fail("Reason: " + result[1]);
            }
        } catch (Throwable t) {
            Log.w(TAG, "testSubscribeSelfTopic unable to get current user: ", t);
            fail("" + t);
        }
    }

    @Test
    public void testStopListeningToMyMessages() {
        try {
            final CountDownLatch latch = new CountDownLatch(2);
            final Object[] result = {false, ""};

            MASUser.getCurrentUser().startListeningToMyMessages(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    result[0] = true;
                    MASUser.getCurrentUser().stopListeningToMyMessages(new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void object) {
                            result[0] = true;
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable e) {
                            result[1] = "Cant subscribe to self topic ";
                            latch.countDown();
                        }
                    });

                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[1] = "Can't stop listening before starting.";
                    latch.countDown();
                }
            });

            await(latch);
            if (!(boolean) result[0]) {
                Log.w(TAG, "testSubscribeSelfTopic onError: " + result[1]);
                fail("Reason: " + result[1]);
            }
        } catch (Throwable t) {
            Log.w(TAG, "testSubscribeSelfTopic unable to get current user: ", t);
            fail("" + t);
        }
    }

    /**
     * This callback is for listening to messages on a topic
     */
    public MASCallback getMASCallback() {
        try {
            MASCallback<Void> callback = new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                }

                @Override
                public void onError(Throwable e) {
                    Log.w(TAG, "getMASCallback onError " + e + "!!");
                }
            };

            return callback;
        } catch (Exception noCallback) {
            Log.w(TAG, "getMASCallback exception " + noCallback + "!!");
        }

        return null;
    }

    /**
     * This will configure topic listeners, currently only for current user
     */
    public MASTopic configureTopicListeners(MASCallback callback) {

        // create the user topic listener
        try {
            String id = MASUser.getCurrentUser().getId();
            final MASTopic masTopic = new MASTopicBuilder().setUserId(id).setCustomTopic(id).build();

            MASUser.getCurrentUser().startListeningToTopic(masTopic, callback);

            return masTopic;
        } catch (MASException me) {
            Log.w(TAG, "configureTopicListeners current user exception " + me + "!!");
        }

        // create the user topic listener
        try {
            String id = MASUser.getCurrentUser().getId();
            final MASTopic masTopic = new MASTopicBuilder().setUserId(id).setCustomTopic(TOPIC_NAME).build();

            MASUser.getCurrentUser().startListeningToTopic(masTopic, callback);

            return masTopic;
        } catch (MASException me) {
            Log.w(TAG, "configureTopicListeners " + TOPIC_NAME + " exception " + me + "!!");
        }

        return null;
    }

    /**
     * Set up a message receiver to verify that message was sent
     */
    protected BroadcastReceiver initMessageReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED)) {
                        try {
                            MASMessage message = MASMessage.newInstance(intent);
                            final String senderId = message.getSenderId();
                            final String contentType = message.getContentType();
                            if (contentType.startsWith("image")) {
                                byte[] msg = message.getPayload();
                                Log.w(TAG, "message receiver got image from " + senderId + ", image length " + msg.length);
                            } else {
                                byte[] msg = message.getPayload();
                                final String m = new String(Base64.decode(msg, Base64.NO_WRAP));
                                Log.w(TAG, "message receiver got text message from " + senderId + ", " + m);
                            }
                        } catch (MASException me) {
                            Log.w(TAG, "message receiver exception: " + me);
                        }
                    }
                }
            };

            mActivityRule.getActivity().registerReceiver(receiver, intentFilter);

            return receiver;
        } catch (Exception x) {
            Log.w(TAG, "initMessageReceiver exception: " + x);
        }
        return null;
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
}
