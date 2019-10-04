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
    private byte[] fileBytes;

    public MASFileObject(){

    }

    /**
     * Constructs MASFileObject using file bytes.
     *
     * @param  fileName        The file name.
     * @param  fileType        The mime type, eg. if file is sample.png the fileType should be "image/png".
     * @param  fieldName       The field name for the file, in case of file upload using http post.
     * @param  fileBytes       The file content as bytes.
     */
    public MASFileObject(String fileName, String fileType, String fieldName, byte[] fileBytes){
        this.fileName = fileName;
        this.fileType = fileType;
        this.fieldName = fieldName;
        this.fileBytes = fileBytes;
    }

    /**
     * Constructs MASFileObject using absolute path of file.
     *
     * @param  fileName        The file name.
     * @param  fileType        The mime type, eg. if file is sample.png the fileType should be "image/png".
     * @param  fieldName       The filed name in case of file upload using http post.
     * @param  filePath        Absolute path of the file.
     */
    public MASFileObject(String fileName, String fileType, String fieldName, String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fieldName = fieldName;
    }

    /**
     * Constructs MASFileObject using absolute file uri.
     *
     * @param  fileName        The file name.
     * @param  fileType        The mime type, eg. if file is sample.png the fileType should be "image/png".
     * @param  fieldName       The filed name in case of file upload using http post.
     * @param  fileUri         The file uri.
     */
    public MASFileObject(String fileName, String fileType, String fieldName, Uri fileUri){
        this.fileUri = fileUri;
        this.fileType = fileType;
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

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
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
