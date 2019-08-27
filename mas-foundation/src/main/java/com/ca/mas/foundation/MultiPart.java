package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPart {

    private List<MASFileObject> filePart = new ArrayList<>();
    private Map<String, String> formFields = new HashMap<>();

    public List<MASFileObject> getFilePart() {
        return filePart;
    }

    public void addFilePart(@NonNull MASFileObject file) {
        filePart.add(file);
    }

    public void addFormField(String key, String value) {
        formFields.put(key, value);
    }
    public Map<String, String> getFormFields() {
        return formFields;
    }

    public void reset() {

        filePart.clear();
        formFields.clear();

    }
}
