/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.token.JWTRS256Validator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.content.Context.MODE_PRIVATE;
import static com.ca.mas.GatewayDefaultDispatcher.MULTIPART;
import static com.ca.mas.foundation.MAS.TAG;


public class MASJWKSPreloadTest extends MASMockGatewayTestBase {


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @After
    public void clearJWKS(){
        final SharedPreferences prefs;
        prefs = getContext().getSharedPreferences(JWTRS256Validator.JWT_KEY_SET_FILE, MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        JWTRS256Validator.setJwks(null);
    }

    @Test
    public void testJWKSPreloadEnabledAlgHS256() throws Exception {
        MAS.enableJwksPreload(true);
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_hs256.json"));
        Thread.sleep(2000);
        Assert.assertNotNull(JWTRS256Validator.getJwks());

    }

    @Test
    public void testJWKSPreloadEnabledAlgRS256() throws Exception {
        MAS.enableJwksPreload(true);
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_rs256.json"));
        Thread.sleep(2000);
        Assert.assertNotNull(JWTRS256Validator.getJwks());
    }

    @Test
    public void testJWKSPreloadDisabledAlgRS256() throws Exception {
        MAS.enableJwksPreload(false);
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_rs256.json"));
        Thread.sleep(2000);
        Assert.assertNotNull(JWTRS256Validator.getJwks());
    }

    @Test
    public void testJWKSPreloadDisabledAlgHS256() throws Exception {
        MAS.enableJwksPreload(false);
        MAS.start(getContext(),TestUtils.getJSONObject("/msso_config_hs256.json"));
        Assert.assertNull(JWTRS256Validator.getJwks());
    }




    @Test
    public  void temp() throws IOException, JSONException {

        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config.json"));
        URL multipart_uri = new URL(MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                MULTIPART);

        MAGHttpClient client = new MAGHttpClient();
        MASRequest request = new MASRequest.MASRequestBuilder(multipart_uri).
                responseBody(MASResponseBody.jsonBody()).setPublic().build();


        MASResponse<JSONObject> response = client.execute(request);

        JSONObject jsonObject = response.getBody().getContent();
    }

    @Test
    public void testFileByteArrayPost() throws Exception {

        File file = new File(createFile());
        byte[] bytes = fileToBytes(file);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.byteArrayBody(bytes))
                .header("ContentType", "text/plain")
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        callback.get();
    }

    byte[] fileToBytes(File file){
        byte[] bytes = new byte[0];
        try(FileInputStream inputStream = new FileInputStream(file)) {
            bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Test
    public void testNodejsServer() throws Exception {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put("foo", "value1");
        params.put("bar", "value2");
        createFile();

        String result = multipartRequest("http://10.138.161.106:3000", params, "sample.txt", "file", "text/plain");
    }


    @Test
    public void imageFileUpload() throws IOException {

        URL url = new URL("http://10.138.161.30:3000");
        HttpURLConnection connection = null;

        InputStream inputStream = null;

      /*  File file = new File(getImagePath());
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] buffer =   new byte[(int) fileInputStream.getChannel().size()];
        fileInputStream.read(buffer);
        */
        InputStream iStream = getContext().getAssets().open("image1.png");
        byte[] buffer = new byte[2000];
        iStream.read(buffer);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "image/png");
        connection.setRequestMethod("POST");
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(buffer);

        int response = connection.getResponseCode();

    }

    @Test
    public void textFileUpload() throws IOException {

        URL url = new URL("http://10.138.161.106:3000");
        HttpURLConnection connection = null;

        InputStream inputStream = null;

       File file = new File(createFile());
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] buffer =   new byte[(int) fileInputStream.getChannel().size()];
        fileInputStream.read(buffer);

        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "'text/plain'");
        connection.setRequestMethod("POST");
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(buffer);

        int response = connection.getResponseCode();

    }

@Test
public void getCerts() throws IOException, NoSuchAlgorithmException, KeyManagementException {
    URL url = new URL("https://10.138.161.120:8100");
    SSLContext sslCtx = SSLContext.getInstance("TLS");
    sslCtx.init(null, new TrustManager[]{new X509TrustManager() {

        private X509Certificate[] accepted;

        @Override
        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            throw new CertificateException("This trust manager is only for clients");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            accepted = xcs;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return accepted;
        }
    }}, null);

    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

    connection.setSSLSocketFactory(sslCtx.getSocketFactory());
    connection.getResponseCode();
    Certificate[] certificates = connection.getServerCertificates();
    connection.disconnect();

}



    public String multipartRequest(String urlTo, Map<String, String> parmas, String filepath, String filefield, String fileMimeType) throws Exception {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;
        String filename = q[idx];

        try {
            File file = new File(getContext().getFilesDir(), filepath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + filename + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + filename + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + "image/png" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            InputStream iStream = getContext().getAssets().open("image1.png");
            ;//new FileInputStream(getImagePath());
            bytesRead = iStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            Iterator<String> keys = parmas.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = parmas.get(key);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                //outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            if (200 != connection.getResponseCode()) {
                throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            inputStream = connection.getInputStream();

            result = this.convertStreamToString(inputStream);

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            Log.e("TEST",e.getLocalizedMessage());
            throw new Exception(e);
        }

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public String createFile() throws IOException {


        String MEDIA_PATH = new String("/sdcard/DCIM/");
        File home = new File(MEDIA_PATH);

        final String TESTSTRING = new String("Hello Android");

        /* We have to use the openFileOutput()-method
         * the ActivityContext provides, to
         * protect your file from others and
         * This is done for security-reasons.
         * We chose MODE_WORLD_READABLE, because
         *  we have nothing to hide in our file */
        FileOutputStream fOut = getContext().openFileOutput("sample.txt",
                MODE_PRIVATE);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);

        // Write the string to the file
        osw.write(TESTSTRING);

        /* ensure that everything is
         * really written out and close */
        osw.flush();
        osw.close();
        File file = new File(getContext().getFilesDir(), "sample.txt");
        return file.getAbsolutePath();

    }

    public String getImagePath()
    {
        File imageFile = new File("/sdcard/", "ca.png");
        return imageFile.getAbsolutePath();
    }

}
