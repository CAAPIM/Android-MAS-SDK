package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MultiPart {

    private List<FilePart> filePart = new ArrayList<>();
    private FormPart formPart;

    public List<FilePart> getFilePart() {
        return filePart;
    }

    public void addFilePart(@NonNull FilePart file) {
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
