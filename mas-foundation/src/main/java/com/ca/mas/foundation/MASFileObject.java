package com.ca.mas.foundation;

/**
 * <p>The <b>MASFileObject</b> class is a representation of file attributes.</p>
 */
public class MASFileObject {

    private String fileName;
    private String filePath;
    private String fileType;
    private String fieldName;


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
