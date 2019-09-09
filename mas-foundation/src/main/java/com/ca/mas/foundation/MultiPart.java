package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the multipart body of a multipart/form-data http post request.
 * Multipart consists of one or more key-value pair, representing the form fields, and one or more files representing the file part.
 * To create file part see {@link MASFileObject}. Form fields are added as {@link HashMap}.
 */
public class MultiPart {

    private List<MASFileObject> filePart = new ArrayList<>();
    private Map<String, String> formFields = new HashMap<>();

    /* Returns list of MASFileObject. If no files were added the list would be empty.*/
    public List<MASFileObject> getFilePart() {
        return filePart;
    }

    /* Adds a file to the multipart body. */
    public void addFilePart(@NonNull MASFileObject file) {
        filePart.add(file);
    }

    /* Adds a form field to the multipart body. */
    public void addFormField(String key, String value) {
        formFields.put(key, value);
    }

    /* Returns Map containing all the added form fields. */
    public Map<String, String> getFormFields() {
        return formFields;
    }

    /* Removes all files and form fields that were added to multipart.*/
    public void reset() {
        filePart.clear();
        formFields.clear();
    }
}
