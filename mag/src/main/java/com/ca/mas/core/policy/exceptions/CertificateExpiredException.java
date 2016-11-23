package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.registration.RegistrationException;
import com.ca.mas.core.store.TokenStoreException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.cert.X509Certificate;

public class CertificateExpiredException extends RetryRequestException {

    public CertificateExpiredException(String message) {
        super(message);
    }

    public CertificateExpiredException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public void recover(MssoContext context) throws Exception {
        renewDevice(context);
    }

    public void renewDevice(MssoContext mssoContext) throws RegistrationException, TokenStoreException {
        final URI tokenUri = mssoContext.getConfigurationProvider().getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RENEW_DEVICE);

        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(tokenUri);
        builder.header(ServerClient.CERT_FORMAT, ServerClient.PEM);
        builder.put(null);

        MAGHttpClient httpClient = mssoContext.getMAGHttpClient();

        final MAGResponse response;
        String errorMessage = "Unable to renew device: ";
        try {
            response = httpClient.execute(builder.build());
        } catch (IOException e) {
            errorMessage += e.getMessage();
            throw new RegistrationException(MAGErrorCode.DEVICE_NOT_RENEWED, errorMessage, e);
        }

        int responseCode = response.getResponseCode();
        if( responseCode != HttpURLConnection.HTTP_OK ){
            //Perform re-registration
            mssoContext.destroyPersistentTokens();
        } else {
            //Save the new cert
            byte[] chainBytes = response.getBody().getRawContent();
            final X509Certificate[] chain = CertUtils.decodeCertificateChain(chainBytes);
            mssoContext.getTokenManager().saveClientCertificateChain(chain);
            mssoContext.resetHttpClient();
        }
    }

}
