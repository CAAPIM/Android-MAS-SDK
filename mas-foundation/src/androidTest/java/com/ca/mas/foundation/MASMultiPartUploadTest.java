package com.ca.mas.foundation;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.error.MAGRuntimeException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MASMultiPartUploadTest extends MASLoginTestBase {


    @Test
    public void uploadMultipartFormTest() throws Exception, MASException {
        try {

            FilePart filePart = new FilePart();
            FormPart formPart = new FormPart();
            MultiPart multiPart = new MultiPart();

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");

            multiPart.addFormPart(formPart);


            filePart.setFieldName("file1");
            filePart.setFileName("ca.png");
            filePart.setFilePath(getFilePath("ca.png"));
            filePart.setFileType("image/png");

            multiPart.addFilePart(filePart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();
            MAS.upload(request, multiPart, null, callbackFuture);
            callbackFuture.get();



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

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");
            multiPart.addFormPart(formPart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);

            MASResponse result = callbackFuture.get();

            Assert.assertTrue(result.getResponseCode() == 200);

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Test(expected = MAGRuntimeException.class)
    public void uploadMultipartNullTest() throws Exception, MASException {

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, null, null, callbackFuture);

    }

    @Test
    public void uploadFilePartOnlyTest() throws Exception, MASException {
        try {

            MultiPart multiPart = new MultiPart();
            FilePart filepart = new FilePart();
            filepart.setFieldName("file");
            filepart.setFileName("cat.png");
            filepart.setFilePath(getFilePath("cat.png"));
            filepart.setFileType("image/png");
            multiPart.addFilePart(filepart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);

           Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Test
    public void uploadFilePartWithNoFileNameTest() throws Exception, MASException {
        try {

            MultiPart multiPart = new MultiPart();
            FilePart filepart = new FilePart();
            filepart.setFieldName("file");
            //filepart.setFileName("cat.png");
            filepart.setFilePath(getFilePath("cat.png"));
            filepart.setFileType("image/png");
            multiPart.addFilePart(filepart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);

            Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Test(expected = MASException.class)
    public void uploadEmptyMultipartBodyTest() throws MASException, URISyntaxException {

            MultiPart multiPart = new MultiPart();

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, null, callbackFuture);


    }

    @Test
    public void uploadMultipartFormPDFTest() throws Exception, MASException {
        try {

            FilePart filePart = new FilePart();
            FormPart formPart = new FormPart();
            MultiPart multiPart = new MultiPart();

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");

            multiPart.addFormPart(formPart);

            filePart.setFieldName("file1");
            filePart.setFileName("sample.pdf");
            filePart.setFilePath(getFilePath("sample.pdf"));
            filePart.setFileType("application/pdf");

            multiPart.addFilePart(filePart);

            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.UPLOAD)).build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();
            MAS.upload(request, multiPart, null, callbackFuture);
            MASResponse response = callbackFuture.get();

            Assert.assertTrue(response.getResponseCode() == 200);

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
