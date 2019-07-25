package com.ca.mas.foundation;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class MASMultiPartUploadTest extends MASLoginTestBase {


    @Test
    public void uploadMultipartFormTest() throws Exception, MASException {
        try {

            FilePart filePart = new FilePart();
            FormPart formPart = new FormPart();
            MultiPart multiPart = new MultiPart();
            multiPart.setBoundary("==============");

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");

            multiPart.addFormPart(formPart);


            filePart.setFieldName("file1");
            filePart.setFileName("ca.png");
            filePart.setFilePath(getFilePath("ca.png"));
            filePart.setFileType("image/png");

            multiPart.addFilePart(filePart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI("/test/ftp")).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);
            Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);

        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    @Test
    public void uploadFormParamsOnlyTest() throws Exception, MASException {
        try {

            FormPart formPart = new FormPart();
            MultiPart multiPart = new MultiPart();
            multiPart.setBoundary("==============");

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");
            multiPart.addFormPart(formPart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI("/test/ftp")).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);

            Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);



        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    @Test
    public void uploadFilePartOnlyTest() throws Exception, MASException {
        try {

            MultiPart multiPart = new MultiPart();
            multiPart.setBoundary("==============");
            FilePart filepart = new FilePart();
            filepart.setFieldName("file");
            filepart.setFileName("cat.png");
            filepart.setFilePath(getFilePath("cat.png"));
            filepart.setFileType("image/png");
            multiPart.addFilePart(filepart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI("/test/ftp")).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);

           Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);

        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    private String getFilePath(String fileName) throws IOException {
        byte[] bytes = TestUtils.getBytes("/"+fileName);

        String folder = getContext().getFilesDir().getAbsolutePath();
        File file = new File(folder, "ca.png");

        FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(bytes);
        outputStream.close();
        return file.getAbsolutePath();

    }
}
