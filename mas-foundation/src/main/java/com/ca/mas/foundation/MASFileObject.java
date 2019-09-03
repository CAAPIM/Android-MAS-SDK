package com.ca.mas.foundation;

import android.net.Uri;

/**
 * <p>The <b>MASFileObject</b> class is a representation of file attributes.</p>
 */
public class MASFileObject {

    private String fileName;
    private String filePath;
    private String fileType;
    private String fieldName;
    private Uri fileUri;


    public MASFileObject(){

    }
    public MASFileObject(String fileName, String filePath, String fileType, String fieldName){
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fieldName = fieldName;
    }

    public MASFileObject(Uri fileUri, String fileName, String fieldName){
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.fieldName = fieldName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
