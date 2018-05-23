package com.ca.mas.core.datasource;

import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory temporal storage for the DataStorage
 * @param <T>
 */
public class MemoryCache<T> {

    private final long timeToLive;
    private ConcurrentHashMap<String, MemoryCacheObject> concurrentHashMap;

    protected class MemoryCacheObject {
        // TODO: define the object wich will be storaged
        public T value;

        protected MemoryCacheObject(T value) {
            this.value = value;
        }
    }

    /**
     * Starts the lifecycle of MemoryCache with timeToLive seconds for storageTime
     *
     * @param  timeToLive every timeToLive seconds the cache will be cleaned up
     */
    public MemoryCache(final long timeToLive) {
        this.timeToLive = timeToLive * 1000;

        concurrentHashMap = new ConcurrentHashMap();

        if (getTimeToLive() > 0 ) {

            Thread cleanerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(getTimeToLive());
                            clear();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });

            cleanerThread.setDaemon(true);
            cleanerThread.start();
        }
    }

    /**
     *  Seconds to keep alive the cache storage
     * @return long timeToLive
     */
    public long getTimeToLive() {
        return this.timeToLive;
    }


    public void add(String key, T value) {
        concurrentHashMap.put(key, new MemoryCacheObject(value));
    }

    public void remove(String key) {
        concurrentHashMap.remove(key);
    }

    public Object get(String key) {
        Object ret = null;
        if (key == null) {
            return null;
        } else {

            MemoryCacheObject c =  concurrentHashMap.get(key);
            if (c != null) {
                ret = c.value;
            }
        }

        return ret;
    }

    public void clear() {
        Log.i("Clear", "clear has been done");
        concurrentHashMap.clear();
    }

    public long size() {
        return concurrentHashMap.size();
    }
}