/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.cert;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sun.security.pkcs.PKCS10;
import sun.security.x509.X500Signer;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


/**
 * Utility methods for working with certificate and CSRs.
 */
public class CertUtils {

    private static final String PEM_CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END_MARKER = "-----END CERTIFICATE-----";

    /**
     * Decode an X.509 certificate that is encoded as Base-64, with or without PEM "BEGIN CERTIFICATE" markers.
     *
     * @param certificateText the certificate in PEM format, optionally with begin and end markers.
     * @return an X509Certificate instance produced with the default X.509 CertificateFactory.
     * @throws IOException if PEM or Base-64 decoding fails.
     */
    public static X509Certificate decodeCertFromPem(String certificateText) throws IOException {
        int startIndex = certificateText.indexOf(PEM_CERT_BEGIN_MARKER);
        int endIndex = certificateText.indexOf(PEM_CERT_END_MARKER);

        String base64Certificate;
        if ( startIndex < 0 || endIndex < startIndex ) {
            if (endIndex >= 0) throw new IOException("Begin PEM marker present, but end marker missing");
            base64Certificate = certificateText;
        } else {
            base64Certificate = certificateText.substring(
                    startIndex + PEM_CERT_BEGIN_MARKER.length(),
                    endIndex);
        }

        // Remove escaping for all '/' characters in the PEM cert
        base64Certificate = base64Certificate.replace("\\/", "/");
        byte[] bytes = Base64.decode(base64Certificate, Base64.DEFAULT);
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            if (DEBUG) Log.e(TAG, "Unable to decode public certificate, error: " + e + " for cert " + certificateText, e);
            throw new IOException(e);
        }
    }


    /**
     * Generate a PKCS#10 certificate signing request from the specified parameters.
     *
     * @param commonName  the username.  Required.
     * @param deviceId  the device ID.  Required.
     * @param deviceName  the device name.  Required.
     * @param organization  the organization.  Required.
     * @param publicKey  the client's public key.  Required.
     * @param privateKey  the client's private key.  Required.
     * @return a signed PKCS#10 CertificationRequest structure in binary DER format.  Never null.
     * @throws CertificateException if a CSR cannot be created
     */
    public static byte[] generateCertificateSigningRequest(String commonName,
                                                           String deviceId, String deviceName, String organization,
                                                           PublicKey publicKey, PrivateKey privateKey) throws CertificateException {
        try {
            PKCS10 pkcs10 = new PKCS10(publicKey);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            sun.security.x509.X500Name x500Name = new sun.security.x509.X500Name("cn=" + commonName + ", ou=" + deviceId + ", dc=" + deviceName + ", o=" + organization);

            pkcs10.encodeAndSign(new X500Signer(signature, x500Name));
            return pkcs10.getEncoded();
        } catch (Exception t) {
            if (DEBUG) Log.e(TAG, "Unable to generate certificate signing request: " + t, t);
            throw new CertificateException("Unable to generate certificate signing request: " + t);
        }
    }


    /**
     * Convert the specified Certificate array into an X509Certificate array.
     *
     * @param certs certificate array to convert.  Required.
     * @return a same-length array of type X509Certificate[].
     * @throws ClassCastException if at least one certificate is not an X509Certificate.
     */
    private static X509Certificate[] toX509CertArray(Collection<? extends Certificate> certs) {
        List<X509Certificate> x509Certs = new ArrayList<>();
        for (Certificate cert : certs) {
            x509Certs.add((X509Certificate) cert);
        }
        return x509Certs.toArray(new X509Certificate[x509Certs.size()]);
    }

    /**
     * Decode a certificate chain into a an array of X509Certificate instances.
     *
     * @param chainBytes the certificate chain bytes.  Required.
     * @return an array of X509Certificate instances.
     * @throws IllegalArgumentException if the chain cannot be decoded
     */
    public static X509Certificate[] decodeCertificateChain(byte[] chainBytes) {
        try {
            return toX509CertArray(CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(chainBytes)));
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to decode certificate chain: " + e, e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Encode a certificate chain to a byte array.
     * <p/>
     * The returned byte array is simply the encoded form of each certificate appended to the array
     * one by one without any surrounding structure or other delimiters, with the subject cert coming first.
     *
     * @param chain the chain to encode.  Required.
     * @return the encoded bytes of the chain.
     * @throws IllegalArgumentException if the chain cannot be encoded.
     */
    public static byte[] encodeCertificateChain(X509Certificate[] chain) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (X509Certificate cert : chain) {
                baos.write(cert.getEncoded());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


    private CertUtils() {
    }
}
