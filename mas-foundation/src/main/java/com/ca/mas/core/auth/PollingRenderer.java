/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import com.ca.mas.core.service.Provider;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Poll MAG periodically for the Authorization Code
 */
public abstract class PollingRenderer extends AuthRenderer {

    //Error for invalid session ID or Poll URL
    public static final int INVALID_SESSION_ID_OR_POLL_URL = 10;

    /**
     * Error due to errors occurred during request for auth code.
     * The session is expired
     */
    public static final int SESSION_EXPIRED = 11;

    /**
     * Default Poll Interval
     */
    public static final int POLL_INTERVAL = 5;

    /**
     * Default Maximum number of polls
     */
    public static final int MAX_POLL_COUNT = 6;

    /**
     * Default delay for the first execution
     */
    public static final int DELAY = 10;

    private TimerTask task;

    //Start the polling
    private ScheduledExecutorService timer = null;

    @Override
    public boolean init(Context context, Provider provider) {
        if (!super.init(context, provider)) {
            return false;
        }
        if (provider.getPollUrl() == null || !URLUtil.isHttpsUrl(provider.getPollUrl())) {
            onError(INVALID_SESSION_ID_OR_POLL_URL, "Invalid Poll url", null);
            return false;
        }
        if (provider.getUrl() == null || !URLUtil.isHttpsUrl(provider.getUrl())) {
            onError(INVALID_SESSION_ID_OR_POLL_URL, "Invalid session id", null);
            return false;
        }
        return true;
    }

    @Override
    public void onRenderCompleted() {
        if (startPollingOnStartup()) {
            poll();
        }
    }

    @Override
    public void close() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
    }

    /**
     * Start the polling
     */
    protected void poll() {

        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }

        timer = Executors.newScheduledThreadPool(1);

        final long maxPollCount = getMaxPollCount();
        final int[] pollCount = {0};

        task = new TimerTask() {

            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "Polling Authorization code from Server");
                if (pollCount[0]++ < maxPollCount) {
                    proceed();
                } else {
                    onError(SESSION_EXPIRED, "Session expired", null);
                    close();
                }
            }
        };
        timer.scheduleWithFixedDelay(task, getDelay(), getPollInterval(), TimeUnit.SECONDS);
    }

    /**
     * The interval between the termination of one
     * execution and the commencement of the next
     *
     * @return the interval between the termination of one
     * execution and the commencement of the next
     */
    protected long getPollInterval() {
        return POLL_INTERVAL;
    }

    /**
     * The maximum number of polls
     *
     * @return The maximum number of polls
     */
    protected long getMaxPollCount() {
        return MAX_POLL_COUNT;
    }

    /**
     * The time to delay first execution.
     *
     * @return the time to delay first execution.
     */
    protected long getDelay() {
        return DELAY;
    }


    @Override
    protected void onAuthCodeReceived(String code, String state) {
        if (timer != null) {
            timer.shutdown();
        }
    }

    /**
     * Start polling MAG on startup
     *
     * @return True to start polling when after {@link com.ca.mas.core.auth.AuthRenderer#onRenderCompleted}
     */
    protected abstract boolean startPollingOnStartup();

}
