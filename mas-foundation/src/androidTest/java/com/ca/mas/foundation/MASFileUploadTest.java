package com.ca.mas.foundation;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.io.IoUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

public class MASFileUploadTest extends MASLoginTestBase {


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

          // multiPart.addFilePart(filePart);

            FilePart filepart2 = new FilePart();
            filepart2.setFieldName("file2");
            filepart2.setFileName("cat.png");
            filepart2.setFilePath(getFilePath("cat.png"));
            filepart2.setFileType("image/png");


            multiPart.addFilePart(filepart2);
            final MASProgressListener progressListener = new MASProgressListener() {
                @Override
                public void onProgress(String progressPercent) {
                       Assert.assertTrue(Integer.valueOf(progressPercent) >=0);

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void onError(MAGError error) {


                }
            };
////////////////////TO BE REMOVED WHEN END POINT IS AVAILABLE/////////////////
            String cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICVTCCAb4CCQC7x7Kbuo66kjANBgkqhkiG9w0BAQUFADBvMQswCQYDVQQGEwJp\n" +
                    "bjELMAkGA1UECAwCa2ExEjAQBgNVBAcMCWJhbmdhbG9yZTELMAkGA1UECgwCY2Ex\n" +
                    "ETAPBgNVBAMMCCouY2EuY29tMR8wHQYJKoZIhvcNAQkBFhBydXFpaGtAZ21haWwu\n" +
                    "Y29tMB4XDTE4MTEyODA2MjExOVoXDTE4MTIyODA2MjExOVowbzELMAkGA1UEBhMC\n" +
                    "aW4xCzAJBgNVBAgMAmthMRIwEAYDVQQHDAliYW5nYWxvcmUxCzAJBgNVBAoMAmNh\n" +
                    "MREwDwYDVQQDDAgqLmNhLmNvbTEfMB0GCSqGSIb3DQEJARYQcnVxaWhrQGdtYWls\n" +
                    "LmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAv8iU7J5yuirId6M8/9vz\n" +
                    "gaT1BL3YY1AZs7jqDz3KJwuvZ6dHgbisIYQcHLD8VXoLYxY5QW2i1hx5m6YkS8xa\n" +
                    "2bEuagHhqk1QTAxsd/qDTWqbBzWzCscvopM5FSvqZIwjf/Ag8CNzmrQdGN5JD8QT\n" +
                    "HoLytS3GhW11SpKl5VWSfc0CAwEAATANBgkqhkiG9w0BAQUFAAOBgQCf2gQZZ0aW\n" +
                    "A/NlTqgkdpAZBGvWs/tv/bp64sR4upQN7QeLi32D0QJHSJCp9rHgbFxSJv6R6a3E\n" +
                    "sl69ShKywkT3zHa4maocuogymqXG2h1j0IXWGg95mX5clBuG+nDnk3hAw4+Iu2km\n" +
                    "SeM0oOmhJJXERgCCEzvED/8Tw6fUTFWIbw==\n" +
                    "-----END CERTIFICATE-----";

            X509Certificate certificate = CertUtils.decodeCertFromPem(cert);
///////////////////////////////////////////////////////////////////////////

            URL url = new URL("https://10.122.37.208:8100");//Change to endpoint
            MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                    .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort())
                            .build());

            configuration.add(certificate);

            MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

            final MASRequest request = new MASRequest.MASRequestBuilder(url).setPublic().build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.upload(request, multiPart, progressListener, callbackFuture);
            Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);



        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    @Test
    public void uploadFormParamsOnlyTest() throws Exception, MASException {
        try {

            FilePart filePart = new FilePart();
            FormPart formPart = new FormPart();
            MultiPart multiPart = new MultiPart();
            multiPart.setBoundary("==============");

            formPart.addFormField("key1", "value1");
            formPart.addFormField("key2", "value2");
            formPart.addFormField("key3", "value3");
            multiPart.addFormPart(formPart);


////////////////////TO BE REMOVED WHEN END POINT IS AVAILABLE/////////////////
            String cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICVTCCAb4CCQC7x7Kbuo66kjANBgkqhkiG9w0BAQUFADBvMQswCQYDVQQGEwJp\n" +
                    "bjELMAkGA1UECAwCa2ExEjAQBgNVBAcMCWJhbmdhbG9yZTELMAkGA1UECgwCY2Ex\n" +
                    "ETAPBgNVBAMMCCouY2EuY29tMR8wHQYJKoZIhvcNAQkBFhBydXFpaGtAZ21haWwu\n" +
                    "Y29tMB4XDTE4MTEyODA2MjExOVoXDTE4MTIyODA2MjExOVowbzELMAkGA1UEBhMC\n" +
                    "aW4xCzAJBgNVBAgMAmthMRIwEAYDVQQHDAliYW5nYWxvcmUxCzAJBgNVBAoMAmNh\n" +
                    "MREwDwYDVQQDDAgqLmNhLmNvbTEfMB0GCSqGSIb3DQEJARYQcnVxaWhrQGdtYWls\n" +
                    "LmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAv8iU7J5yuirId6M8/9vz\n" +
                    "gaT1BL3YY1AZs7jqDz3KJwuvZ6dHgbisIYQcHLD8VXoLYxY5QW2i1hx5m6YkS8xa\n" +
                    "2bEuagHhqk1QTAxsd/qDTWqbBzWzCscvopM5FSvqZIwjf/Ag8CNzmrQdGN5JD8QT\n" +
                    "HoLytS3GhW11SpKl5VWSfc0CAwEAATANBgkqhkiG9w0BAQUFAAOBgQCf2gQZZ0aW\n" +
                    "A/NlTqgkdpAZBGvWs/tv/bp64sR4upQN7QeLi32D0QJHSJCp9rHgbFxSJv6R6a3E\n" +
                    "sl69ShKywkT3zHa4maocuogymqXG2h1j0IXWGg95mX5clBuG+nDnk3hAw4+Iu2km\n" +
                    "SeM0oOmhJJXERgCCEzvED/8Tw6fUTFWIbw==\n" +
                    "-----END CERTIFICATE-----";

            X509Certificate certificate = CertUtils.decodeCertFromPem(cert);
///////////////////////////////////////////////////////////////////////////

            URL url = new URL("https://10.122.37.208:8100");//Change to endpoint
            MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                    .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort())
                            .build());

            configuration.add(certificate);

            MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

            final MASRequest request = new MASRequest.MASRequestBuilder(url).setPublic().build();

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

            FilePart filePart = new FilePart();
            MultiPart multiPart = new MultiPart();
            multiPart.setBoundary("==============");
            FilePart filepart2 = new FilePart();
            filepart2.setFieldName("file1");
            filepart2.setFileName("cat.png");
            filepart2.setFilePath(getFilePath("cat.png"));
            filepart2.setFileType("image/png");
            multiPart.addFilePart(filepart2);


////////////////////TO BE REMOVED WHEN END POINT IS AVAILABLE/////////////////
            String cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICVTCCAb4CCQC7x7Kbuo66kjANBgkqhkiG9w0BAQUFADBvMQswCQYDVQQGEwJp\n" +
                    "bjELMAkGA1UECAwCa2ExEjAQBgNVBAcMCWJhbmdhbG9yZTELMAkGA1UECgwCY2Ex\n" +
                    "ETAPBgNVBAMMCCouY2EuY29tMR8wHQYJKoZIhvcNAQkBFhBydXFpaGtAZ21haWwu\n" +
                    "Y29tMB4XDTE4MTEyODA2MjExOVoXDTE4MTIyODA2MjExOVowbzELMAkGA1UEBhMC\n" +
                    "aW4xCzAJBgNVBAgMAmthMRIwEAYDVQQHDAliYW5nYWxvcmUxCzAJBgNVBAoMAmNh\n" +
                    "MREwDwYDVQQDDAgqLmNhLmNvbTEfMB0GCSqGSIb3DQEJARYQcnVxaWhrQGdtYWls\n" +
                    "LmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAv8iU7J5yuirId6M8/9vz\n" +
                    "gaT1BL3YY1AZs7jqDz3KJwuvZ6dHgbisIYQcHLD8VXoLYxY5QW2i1hx5m6YkS8xa\n" +
                    "2bEuagHhqk1QTAxsd/qDTWqbBzWzCscvopM5FSvqZIwjf/Ag8CNzmrQdGN5JD8QT\n" +
                    "HoLytS3GhW11SpKl5VWSfc0CAwEAATANBgkqhkiG9w0BAQUFAAOBgQCf2gQZZ0aW\n" +
                    "A/NlTqgkdpAZBGvWs/tv/bp64sR4upQN7QeLi32D0QJHSJCp9rHgbFxSJv6R6a3E\n" +
                    "sl69ShKywkT3zHa4maocuogymqXG2h1j0IXWGg95mX5clBuG+nDnk3hAw4+Iu2km\n" +
                    "SeM0oOmhJJXERgCCEzvED/8Tw6fUTFWIbw==\n" +
                    "-----END CERTIFICATE-----";

            X509Certificate certificate = CertUtils.decodeCertFromPem(cert);
///////////////////////////////////////////////////////////////////////////

            URL url = new URL("https://10.122.37.208:8100");//Change to endpoint
            MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                    .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort())
                            .build());

            configuration.add(certificate);

            MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

            final MASRequest request = new MASRequest.MASRequestBuilder(url).setPublic().build();

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
