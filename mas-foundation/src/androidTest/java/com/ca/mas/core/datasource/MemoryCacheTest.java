package com.ca.mas.core.datasource;

import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.TargetApi;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MemoryCacheTest {

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testWriteString() {
        MemoryCache cache = new MemoryCache(200);

        cache.add("eBay", "eBay");
        cache.add("Paypal", "Paypal");
        cache.add("Google", "Google");
        cache.add("Microsoft", "Microsoft");
        cache.add("IBM", "IBM");
        cache.add("Facebook", "Facebook");
        assertEquals(6, cache.size());

        cache.remove("IBM");
        assertEquals(5, cache.size());

        cache.add("Twitter", "Twitter");
        cache.add("SAP", "SAP");
        assertEquals(7, cache.size());

        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    public void testWriteOtherObjects() {
        MemoryCache cache = new MemoryCache(200);

        cache.add("eBay", new Object());
        cache.add("Paypal", new Object());
        cache.add("Google", new Object());
        cache.add("Microsoft", 6);
        cache.add("IBM", new Object());
        cache.add("Facebook", new HashMap<>());
        assertEquals(6, cache.size());

        cache.remove("IBM");
        assertEquals(5, cache.size());

        cache.add("Twitter", "Twitter");
        cache.add("SAP", "SAP");
        assertEquals(7, cache.size());

        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    public void testCheckMemoryEmpthyAfterTimelife() throws InterruptedException {
        int timeToLive = 2;
        MemoryCache cache = new MemoryCache(timeToLive);

        cache.add("eBay", new Object());
        cache.add("Paypal", new Object());
        cache.add("Google", new Object());
        cache.add("Microsoft", 6);
        cache.add("IBM", new Object());
        cache.add("Facebook", new HashMap<>());

        Thread.sleep(cache.getTimeToLive() + 10);
        assertEquals(0, cache.size());
    }

    @Test
    public void testGetKey() {
        int timeToLive = 20;
        MemoryCache cache = new MemoryCache(timeToLive);
        Object obj1 = new Object();
        String obj2 = "objj2";

        cache.add("obj1", obj1);
        cache.add("string", obj2);

        assertEquals(obj1, cache.get("obj1"));
        assertEquals(obj2, cache.get("string"));

    }

    @Test
    public void testRemoveKey() {
        int timeToLive = 20;
        MemoryCache cache = new MemoryCache(timeToLive);
        Object obj1 = new Object();
        String obj2 = "objj2";

        cache.add("obj1", obj1);
        cache.add("string", obj2);

        cache.remove("obj1");
        assertEquals(null, cache.get("obj1"));
        assertEquals(1, cache.size());
    }
}
