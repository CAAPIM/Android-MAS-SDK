package com.ca.mas.core.storage;

import android.support.annotation.NonNull;

public interface StorageActions {
    void save(@NonNull String key, String value);
    void save(@NonNull String key, byte[] valeue);
    void delete(@NonNull String key);
    String getString(@NonNull String key);
    byte[] getBytes(@NonNull String key);
}
