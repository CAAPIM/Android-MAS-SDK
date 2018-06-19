package com.ca.mas.core.storage.storagesource;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.ca.mas.core.storage.StorageActions;
import com.ca.mas.foundation.MAS;

public class SharedPreferencesUtil implements StorageActions {

    // - same as the account_name for the AndroidAccount Manager
    private String prefsName;
    private Context context;
    private SharedPreferences sharedpreferences;

    public SharedPreferencesUtil(@NonNull Context ctx, String prefs_name) {
        this.prefsName = prefs_name;
        this.context = MAS.getContext();
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
        sharedpreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    @Override
    public String getString(@NonNull String key) {
        sharedpreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        String retrieve = sharedpreferences.getString(key, null);
        return retrieve;
    }

    @Override
    public byte[] getBytes(@NonNull String key) {
        sharedpreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        String retrieve = sharedpreferences.getString(key, null);

        if (retrieve != null) {
            return retrieve.getBytes();
        }

        return null;
    }

    @Override
    public void delete(@NonNull String key) {
        sharedpreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}

