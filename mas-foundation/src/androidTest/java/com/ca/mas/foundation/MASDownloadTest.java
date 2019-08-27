package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.error.MAGRuntimeException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;

public class MASDownloadTest extends MASLoginTestBase {

    private MASSecurityConfiguration.Builder configuration;

    @Test(expected = MAGRuntimeException.class)
    public void downloadMissingHeaderTest() throws Exception, MASException {
            MASFileObject fileObject = new MASFileObject();
            fileObject.setFileName("ca.png");
            fileObject.setFilePath("/sdcard");


            final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.DOWNLOAD))
                    .responseBody(MASResponseBody.fileBody())
                    //.header("request-type", "download")
                    .build();

            MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

            MAS.download(request, callbackFuture, fileObject, null);

    }
    @Test(expected = MAGRuntimeException.class)
    public void downloadMissingFilenameTest() throws Exception, MASException {
        MASFileObject fileObject = new MASFileObject();
        //fileObject.setFileName("ca.png");
        fileObject.setFilePath("/sdcard");


        final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.DOWNLOAD))
                .responseBody(MASResponseBody.fileBody())
                .header("request-type", "download")
                .build();

        MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

        MAS.download(request, callbackFuture, fileObject, null);

    }

    @Test(expected = MAGRuntimeException.class)
    public void downloadMissingFilepathTest() throws Exception, MASException {
        MASFileObject fileObject = new MASFileObject();
        fileObject.setFileName("ca.png");
       // fileObject.setFilePath("/sdcard");


        final MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.DOWNLOAD))
                .responseBody(MASResponseBody.fileBody())
                .header("request-type", "download")
                .build();

        MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

        MAS.download(request, callbackFuture, fileObject, null);

    }

    @Test
    public void downloadFileExternalTest() throws Exception, MASException {
        MASFileObject fileObject = new MASFileObject();
        fileObject.setFileName("ca.png");
        fileObject.setFilePath(getContext().getFilesDir().getAbsolutePath());


        URL url = new URL("https://homepages.cae.wisc.edu:443/~ece533/images/pool.png");
        final MASRequest request = new MASRequest.MASRequestBuilder(url)
                .responseBody(MASResponseBody.fileBody())
                .header("request-type", "download")

                .build();


        configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort())
                        .build());
        addCert();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

        MASCallbackFuture<MASResponse> callbackFuture = new MASCallbackFuture();

        MAS.download(request, callbackFuture, fileObject, null);
        Assert.assertTrue(callbackFuture.get().getResponseCode() == 200);

    }

    public void addCert() throws IOException {
        String certText = null;
        certText = "-----BEGIN CERTIFICATE-----\n" +
                "MIIH+DCCBuCgAwIBAgIQZVD1HOfHV7H/Coo2NziBujANBgkqhkiG9w0BAQsFADB2\n" +
                "MQswCQYDVQQGEwJVUzELMAkGA1UECBMCTUkxEjAQBgNVBAcTCUFubiBBcmJvcjES\n" +
                "MBAGA1UEChMJSW50ZXJuZXQyMREwDwYDVQQLEwhJbkNvbW1vbjEfMB0GA1UEAxMW\n" +
                "SW5Db21tb24gUlNBIFNlcnZlciBDQTAeFw0xOTA3MTUwMDAwMDBaFw0yMTA3MTQy\n" +
                "MzU5NTlaMIGwMQswCQYDVQQGEwJVUzEOMAwGA1UEERMFNTM3MDYxCzAJBgNVBAgT\n" +
                "AldJMRAwDgYDVQQHEwdNYWRpc29uMSAwHgYDVQQJExcxMjEwIFdlc3QgRGF5dG9u\n" +
                "IFN0cmVldDEoMCYGA1UEChMfVW5pdmVyc2l0eSBvZiBXaXNjb25zaW4tTWFkaXNv\n" +
                "bjENMAsGA1UECxMET0NJUzEXMBUGA1UEAwwOKi5jYWUud2lzYy5lZHUwggIiMA0G\n" +
                "CSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDog638PdY9DMcYflHv38Bre6KDQgvt\n" +
                "vW5KqzLamIuJApzpAeHIQOxucJK2HDfviF729dYwdWIFNBCEo9j3oCgsz7i6ZXmg\n" +
                "04yKQ4I9irfel3hx1GDhzvEqzrXhyapZdy4BwgV+T71pfmre0CPcrmZPG6wY+I1G\n" +
                "nstVVKSyZUS7Zb0QV1Eozrk/jLevOFedkboDKOghK9zRhUS//esXVb8iIBR5jxvT\n" +
                "17v99Ux/VsUSqd80nKecixi47iXB0xrHxurjZZDRN1L4wzHTVNEuMdTzvvD2TpBz\n" +
                "LNa77Khet5wBABvLuhu1baEOUUB57fHEYC4gFoZSRGu7k2mnuujSj9WcDPUo6bJA\n" +
                "SVMPxv3/tPfy4trF8tpYf15P5qqE4wxhwipcB1hrgUB4XTe0bzq2n5kJCtXR21H1\n" +
                "ubOdtU1iaNRYjQ3cn8uq5TgW6YBZfMIZFX+c4OdCdbZ2x6In1sYE7MruKSvl0dyl\n" +
                "zDYfhk829/cpedYCMvXh4yjgnNqxT/mQ1lEmJ1SwT37dLeq5VTMDHw3Vu07AFX+C\n" +
                "i1di3tsMJnElLAJHX9cjHnFuiEnLv7n5yd20Xo3kmoOJP8AORg9HAc2lmL7hynyB\n" +
                "9e8wmHA1Bc+MIS8kCT2LifO+iASG0v1eonLoVUNhqSJP7V1TjAkdcbAoHbBgIXIs\n" +
                "+hzKqmkMqxGCRwIDAQABo4IDRTCCA0EwHwYDVR0jBBgwFoAUHgWjd49sluJbh0um\n" +
                "tIascQAM5zgwHQYDVR0OBBYEFIntQB+z5iomAmBHhnOVKIQ9gZV3MA4GA1UdDwEB\n" +
                "/wQEAwIFoDAMBgNVHRMBAf8EAjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEF\n" +
                "BQcDAjBnBgNVHSAEYDBeMFIGDCsGAQQBriMBBAMBATBCMEAGCCsGAQUFBwIBFjRo\n" +
                "dHRwczovL3d3dy5pbmNvbW1vbi5vcmcvY2VydC9yZXBvc2l0b3J5L2Nwc19zc2wu\n" +
                "cGRmMAgGBmeBDAECAjBEBgNVHR8EPTA7MDmgN6A1hjNodHRwOi8vY3JsLmluY29t\n" +
                "bW9uLXJzYS5vcmcvSW5Db21tb25SU0FTZXJ2ZXJDQS5jcmwwdQYIKwYBBQUHAQEE\n" +
                "aTBnMD4GCCsGAQUFBzAChjJodHRwOi8vY3J0LnVzZXJ0cnVzdC5jb20vSW5Db21t\n" +
                "b25SU0FTZXJ2ZXJDQV8yLmNydDAlBggrBgEFBQcwAYYZaHR0cDovL29jc3AudXNl\n" +
                "cnRydXN0LmNvbTAZBgNVHREEEjAQgg4qLmNhZS53aXNjLmVkdTCCAX8GCisGAQQB\n" +
                "1nkCBAIEggFvBIIBawFpAHcA9lyUL9F3MCIUVBgIMJRWjuNNExkzv98MLyALzE7x\n" +
                "ZOMAAAFr9pw5xgAABAMASDBGAiEAi5AEBMUHpGlv+JVoTlUs42UH0A2MQqwEkYWB\n" +
                "CLn8gFkCIQC32Hhl86Yx/6n7LFhFRSBU5LWW0xG1YQicmzcNYucm1wB2AESUZS6w\n" +
                "7s6vxEAH2Kj+KMDa5oK+2MsxtT/TM5a1toGoAAABa/acOeoAAAQDAEcwRQIgajvf\n" +
                "WF4wI+eQ3oC+CxfX6djCKGXh1OD/AQTMH6MK1JQCIQC7eAGczlROzkMqtMy5BOAl\n" +
                "dBydgGYiOZpMf88HW4Zp2wB2AG9Tdqwx8DEZ2JkApFEV/3cVHBHZAsEAKQaNsgia\n" +
                "N9kTAAABa/acOeYAAAQDAEcwRQIgDP1DaSujp5vZpqBi7Ue1AIvx7GDLo1w67Yr1\n" +
                "4Ytuwo4CIQDHdmsX+bVYtf0dhVmP+UXPPqeeYO5zdK7j8D8Ta7vuezANBgkqhkiG\n" +
                "9w0BAQsFAAOCAQEARWw65fPWFBm72EMX3VQdy4iAEEEw878fRs0iSJA5P7bu350G\n" +
                "xL0+RhINUIPRXgJlWgh4x1wJMpoiB7NHJt4nrUasbg8Kd7G/kVhMeahJFucbp2Nb\n" +
                "xbX0In+HJ/4RDYmOOT9UFnFef03IKhoUxVBiWFFbpkGaPY02/v8cjxTy0IGBY5uj\n" +
                "uF/8d60iIhXCf685LVY7tMAnxmvtyh0A33ZGiB+v3cBrAlN3NOj/ZJ5v1klS/UeY\n" +
                "ELVeZfWveKvzftuxfUHFl508MusRHQNfvBPUyuSadDLzoKt+FNWtSTiAA7uo7ttW\n" +
                "3nGtmFH9FFuuj+d3Dg4tKorSBoy1X8dsxHCh5w==\n" +
                "-----END CERTIFICATE-----";
        X509Certificate cert = CertUtils.decodeCertFromPem(certText);
        configuration.add(cert);
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());
    }


}
