package com.ca.mas.core.storage.sharedstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.foundation.MAS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.TAG;

public class SharedPreferencesUtil implements StorageActions {

    // - same as the account_name for the AndroidAccount Manager
    private String prefsName;
    private Context context;
    private SharedPreferences sharedpreferences;

    public SharedPreferencesUtil(String prefsName) {

        if (prefsName == null || prefsName.isEmpty()) {
            return;
        }

        this.prefsName = prefsName;
        this.context = MAS.getContext();
        sharedpreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    @Override
    public void save(@NonNull String key, byte[] value) {
        put(key, value == null ? "" : Base64.encodeToString(value, Base64.DEFAULT));
    }

    @Override
    public void save(@NonNull String key, String value) {
        put(key,value);
    }

    private void put(@NonNull String key, String value){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    @Override
    public String getString(@NonNull String key) {
        return sharedpreferences.getString(key, null);
    }

    @Override
    public byte[] getBytes(@NonNull String key) {
        String retrieve = sharedpreferences.getString(key, null);

        if (retrieve != null) {
            return retrieve.getBytes();
        }

        return null;
    }

    @Override
    public List<String> getKeys() {
        Map<String, ?> allEntries = sharedpreferences.getAll();

        List<String> retData = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            retData.add(entry.getKey());
        }

        return retData;
    }

    @Override
    public void delete(@NonNull String key) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    @Override
    public void removeAll() {
        Log.d(TAG,"Escalation SharedPreferencesUtil removeAll");
        List<String> keys = getKeys();

        for (String key:keys) {
            delete(key);
        }
    }
}

