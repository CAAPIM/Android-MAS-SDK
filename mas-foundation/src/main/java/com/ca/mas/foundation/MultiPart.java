package com.ca.mas.foundation;

import java.util.ArrayList;
import java.util.List;

public class MultiPart {

    private List<FilePart> filePart = new ArrayList<>();
    private FormPart formPart;
    private String boundary;

    public List<FilePart> getFilePart() {
        return filePart;
    }

    public void addFilePart(FilePart file) {
        filePart.add(file);
    }

    public FormPart getFormPart() {
        return formPart;
    }

    public void addFormPart(FormPart formPart) {
        this.formPart = formPart;
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }
}
