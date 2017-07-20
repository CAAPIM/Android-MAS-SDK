/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * An executor service to manage the thread launching mechanism for {@link MssoService}.
 */
class MssoExecutorService {

    //Values based on {@link android.os.AsyncTask}'s implementation
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final BlockingQueue<Runnable> POOL_WORK_QUEUE = new LinkedBlockingQueue<>(128);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger increment = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            int count = increment.getAndIncrement();
            if (DEBUG) Log.w(TAG, "MssoService thread " + count + " created.");
            return new Thread(r, "MssoServiceThread" + count);
        }
    };

    private static ExecutorService executor;

    public static ExecutorService getInstance() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                            TimeUnit.SECONDS, POOL_WORK_QUEUE, THREAD_FACTORY);
        }
        return executor;
    }

    private MssoExecutorService() {
    }

}
