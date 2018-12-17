package com.ca.mas.foundation;

public class FileField {
    private String fieldName;
    private String filePath;
    private String fileName;
    private String fileType;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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

    public String getFileName() {
        String[] q = getFilePath().split("/");
        int idx = q.length - 1;
        fileName = q[idx];
        return fileName;
    }

}
