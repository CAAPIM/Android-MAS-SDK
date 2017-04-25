package com.ca.mas;

import com.ca.mas.core.io.IoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TestUtils {

    private static final int DEFAULT_MAX = 10485760;

    public static JSONObject getJSONObject(String path) throws IOException, JSONException {
        path = path + ".json";
        byte[] bytes = IoUtils.slurpStream(TestUtils.class.getResourceAsStream(path), DEFAULT_MAX);
        return new JSONObject(new String(bytes));

    }
}
