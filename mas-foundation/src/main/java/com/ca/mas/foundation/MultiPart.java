package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MultiPart {

    private List<MASFileObject> filePart = new ArrayList<>();
    private FormPart formPart;

    public List<MASFileObject> getFilePart() {
        return filePart;
    }

    public void addFilePart(@NonNull MASFileObject file) {
        filePart.add(file);
    }

    public FormPart getFormPart() {
        return formPart;
    }

    public void addFormPart(@NonNull FormPart formPart) {
        this.formPart = formPart;
    }

    public void reset() {
        if(filePart != null) {
            filePart.clear();
        }
        if(formPart != null){
            formPart.clear();
        }
    }
}
