/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.cert;

import android.util.Base64;


import com.ca.mas.core.io.IoUtils;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents an SHA-256 fingerprint of a public key from a trusted CA cert.  This is used to prevent man-in-the-middle
 * attacks by a compromised/hostile/government-compelled CA by way of checking whether the trust anchor's public key
 * is from a recognized list.
 * <p/>
 * To create an instance of this class use the {@link #fromPublicKey} or {@link #fromHashString(String, int)} method.
 */
public class PublicKeyHash implements Serializable {
    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("[a-f0-9]{64}");

    private final byte[] hash;

    private PublicKeyHash(byte[] hash) {
        this.hash = hash;
    }

    /**
     * Create a new key hash from the specified public key.
     *
     * @param publicKey the public key to examine.  Required.
     * @return a new key hash.  Never null.
     * @throws IllegalArgumentException if the public key's encoding format is not "X.509".
     */
    public static PublicKeyHash fromPublicKey(PublicKey publicKey) {
        return new PublicKeyHash(toHash(publicKey));
    }

    /**
     * Create a new key hash from the specified public key.
     *
     * @param certificate the certificate whose key hash to compute.  Required.
     * @return a new key hash.  Never null.
     * @throws IllegalArgumentException if the public key's encoding format is not "X.509".
     */
    public static PublicKeyHash fromCertificate(Certificate certificate) {
        return fromPublicKey(certificate.getPublicKey());
    }


    /**
     * Create a new key hash from the specified hash string.
     *
     * @param hashString the base64 hash string.
     * @param flags      controls certain features of the decoded output.
     *                   Pass {@link Base64#DEFAULT} to decode standard Base64.
     * @return a new key hash.  Never null.
     * @throws IllegalArgumentException if the hash string is not in the expected format.
     */
    public static PublicKeyHash fromHashString(String hashString, int flags) {
        //For backward compatibility
        if (SHA256_HEX_PATTERN.matcher(hashString).matches()) {
            return new PublicKeyHash(IoUtils.hexToByteArray(hashString));
        } else {
            return new PublicKeyHash(Base64.decode(hashString, flags));
        }
    }


    /**
     * Check if this key hash matches the specified key hash.
     *
     * @param otherHash the other key hash, as a lowercase hex dump of an SHA-256 hash of the SubjectPublicKeyInfo structure.
     * @return true if this hash matches the other hash.
     */
    public boolean matches(byte[] otherHash) {
        return hash != null && Arrays.equals(hash, otherHash);
    }

    /**
     * Check if this key hash matches that of the specified public key.
     *
     * @param publicKey the public key to compare against.  Required.
     * @return true if this hash matches that key's hash.
     */
    public boolean matches(PublicKey publicKey) {
        return matches(toHash(publicKey));
    }

    /**
     * Check if this key hash matches that of the public key in the specified certificate.
     *
     * @param certificate the certificate whose public key to compare against.  Required.
     * @return true if this hash matches that certificate's public key's hash.
     */
    public boolean matches(X509Certificate certificate) {
        return matches(certificate.getPublicKey());
    }

    /**
     * @return the key hash as an SHA-256 Base64 string.
     */
    public String getHashString() {
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    /**
     * Get the key hash for the specified public key.
     *
     * @param publicKey a public key.  Required.
     * @return a key hash
     * @throws IllegalArgumentException if the public key's encoding format is not "X.509".
     */
    private static byte[] toHash(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null || encoded.length < 1)
            throw new IllegalArgumentException("public key cannot be encoded");

        String format = publicKey.getFormat();
        if (!"X.509".equalsIgnoreCase(format) && !"x509".equalsIgnoreCase(format))
            throw new IllegalArgumentException("public key encoding format is not X.509");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(encoded);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicKeyHash that = (PublicKeyHash) o;

        return Arrays.equals(hash, that.hash);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
}
