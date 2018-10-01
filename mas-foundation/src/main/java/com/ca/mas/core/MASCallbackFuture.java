package com.ca.mas.core;

import android.os.Handler;

import com.ca.mas.foundation.MASCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MASCallbackFuture<T> extends MASCallback<T> implements Future<T> {

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private boolean done = false;
    private T result;
    private Throwable throwableResult;
    private Handler handler;

    public MASCallbackFuture() {
    }

    public MASCallbackFuture(Handler handler) {
        this.handler = handler;
    }

    public void reset() {
        this.result = null;
        this.throwableResult = null;
        this.done = false;
        this.countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void onSuccess(T result) {
        this.result = result;
        this.done = true;
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable e) {
        this.throwableResult = e;
        this.done = true;
        countDownLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!done) {
            countDownLatch.await();
        }
        if (throwableResult != null) {
            throw new ExecutionException(throwableResult);
        }
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            countDownLatch.await(timeout, unit);
        }
        if (throwableResult != null) {
            throw new ExecutionException(throwableResult);
        }
        return result;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }
}
