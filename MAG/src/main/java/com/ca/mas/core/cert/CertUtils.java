/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.cert;

import android.util.Base64;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Set;
import org.spongycastle.asn1.DERSet;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.X509Extensions;
import org.spongycastle.jce.PKCS10CertificationRequest;
import org.spongycastle.jce.X509KeyUsage;
import org.spongycastle.x509.X509V3CertificateGenerator;
import org.spongycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.spongycastle.x509.extension.SubjectKeyIdentifierStructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.security.auth.x500.X500Principal;

/**
 * Utility methods for working with certificate and CSRs.
 */
public class CertUtils {

    static final String PEM_CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    static final String PEM_CERT_END_MARKER = "-----END CERTIFICATE-----";

    // Key usage bits (as used by bouncy castle; or them together to make a key usage)
    static final int KU_encipherOnly = 1;
    static final int KU_cRLSign = 2;
    static final int KU_keyCertSign = 4;
    static final int KU_keyAgreement = 8;
    static final int KU_dataEncipherment = 16;
    static final int KU_keyEncipherment = 32;
    static final int KU_nonRepudiation = 64;
    static final int KU_digitalSignature = 128;
    static final int KU_decipherOnly = 32768;

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
            throw new IOException(e);
        }
    }


    /**
     * Generate a self-signed certificate.
     *
     * @param dn subject DN.  Required.
     * @param subjectPublicKey public key to "certify" by encoding it into the cert.  Required.
     * @param issuerPrivateKey private key with which to sign the cert.  Required.
     * @param random a SecureRandom instance to use for choosing a serial number.
     * @return a new X509Certificate instance.  Never null.
     * @throws CertificateException if a cert cannot be generated.
     */
    public static X509Certificate generateSelfSignedCertificate(String dn, PublicKey subjectPublicKey, PrivateKey issuerPrivateKey, SecureRandom random) throws CertificateException {
        X500Principal subjectDn = new X500Principal(dn);
        String sigAlg = "SHA1withRSA";
        int daysUntilExpiry = 10 * 365;
        Date notBefore = new Date(new Date().getTime() - (10 * 60 * 1000L)); // 10 min ago
        Date notAfter = new Date(notBefore.getTime() + (daysUntilExpiry * 24 * 60 * 60 * 1000L)); // daysUntilExpiry days after notBefore
        BigInteger serialNumber = new BigInteger(64, random).abs();

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        certgen.setSerialNumber(serialNumber);
        certgen.setNotBefore(notBefore);
        certgen.setNotAfter(notAfter);
        certgen.setSignatureAlgorithm(sigAlg);
        certgen.setSubjectDN(subjectDn);
        certgen.setIssuerDN(subjectDn);
        certgen.setPublicKey(subjectPublicKey);

        certgen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        certgen.addExtension(X509Extensions.KeyUsage, true, new X509KeyUsage(KU_digitalSignature | KU_keyEncipherment));

        try {
            certgen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(subjectPublicKey));
            certgen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(subjectPublicKey));

            return certgen.generate(issuerPrivateKey);
        } catch (Exception e) {
            throw new CertificateException("Unable to generate self-signed cert: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a PKCS#10 certificate signing request from the specified parameters.
     *
     * @param commonName  the username.  Required.
     * @param deviceId  the device ID.  Required.
     * @param deviceName  the device name.  Required.
     * @param organization  the organization.  Required.
     * @param keyPair  the client's public and private key pair.  Required.
     * @return a signed PKCS#10 CertificationRequest structure in binary DER format.  Never null.
     * @throws CertificateException if a CSR cannot be created
     */
    public static byte[] generateCertificateSigningRequest(String commonName, String deviceId, String deviceName, String organization, KeyPair keyPair) throws CertificateException {
        try {
            X500Principal subject = new X500Principal("cn=" + commonName + ", ou=" + deviceId + ", dc=" + deviceName + ", o=" + organization);
            ASN1Set attrs = new DERSet(new ASN1EncodableVector());
            PKCS10CertificationRequest csr = new PKCS10CertificationRequest("SHA1withRSA", subject, keyPair.getPublic(), attrs, keyPair.getPrivate(), null);
            return csr.getEncoded();
        } catch (Exception e) {
            throw new CertificateException("Unable to generate certificate signing request: " + e.getMessage(), e);
        }
    }


    /**
     * Convert the specified Certificate array into an X509Certificate array.
     *
     * @param certs certificate array to convert.  Required.
     * @return a same-length array of type X509Certificate[].
     * @throws ClassCastException if at least one certificate is not an X509Certificate.
     */
    static X509Certificate[] toX509CertArray(Collection<? extends Certificate> certs) {
        List<X509Certificate> x509Certs = new ArrayList<X509Certificate>();
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
