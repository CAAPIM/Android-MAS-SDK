/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.cert;

import com.ca.mas.core.io.IoUtils;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

/**
 * Represents an SHA-256 fingerprint of a public key from a trusted CA cert.  This is used to prevent man-in-the-middle
 * attacks by a compromised/hostile/government-compelled CA by way of checking whether the trust anchor's public key
 * is from a recognized list.
 * <P/>
 * To create an instance of this class use the {@link #fromPublicKey} or {@link #fromHashString} method.
 */
public class PublicKeyHash implements Serializable {
    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("[a-f0-9]{64}");
    private final String hash;

    private PublicKeyHash(String hash) {
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
     * Create a new key hash from the specified hash string.
     *
     * @param hashString the hash string, eg "8b7df143d91c716ecfa5fc1730022f6b421b05cedee8fd52b1fc65a96030ad52"
     * @return a new key hash.  Never null.
     * @throws IllegalArgumentException if the hash string is not in the expected format.
     */
    public static PublicKeyHash fromHashString(String hashString) {
        final String hash = hashString.trim().toLowerCase();
        if (!SHA256_HEX_PATTERN.matcher(hash).matches())
            throw new IllegalArgumentException("invalid key hash string");
        return new PublicKeyHash(hash);
    }

    /**
     * Check if this key hash matches the specified key hash.
     *
     * @param otherHash the other key hash, as a lowercase hex dump of an SHA-256 hash of the SubjectPublicKeyInfo structure.
     * @return true if this hash matches the other hash.
     */
    public boolean matches(String otherHash) {
        return hash != null && hash.equals(otherHash);
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
     * Get the key hash for the specified trust anchor certificate.
     *
     * @param certificate the certificate whose key hash to compute.  Required.
     * @return the key hash.
     * @throws IllegalArgumentException if the public key's encoding format is not "X.509".
     */
    public static String toHash(X509Certificate certificate) {
        return toHash(certificate.getPublicKey());
    }

    /**
     * @return the key hash as an SHA-256 hex string, eg "8b7df143d91c716ecfa5fc1730022f6b421b05cedee8fd52b1fc65a96030ad52".
     */
    public String getHash() {
        return hash;
    }

    /**
     * Get the key hash for the specified public key.
     *
     * @param publicKey  a public key.  Required.
     * @return a key hash, eg "8b7df143d91c716ecfa5fc1730022f6b421b05cedee8fd52b1fc65a96030ad52".
     * @throws IllegalArgumentException if the public key's encoding format is not "X.509".
     */
    public static String toHash(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null || encoded.length < 1)
            throw new IllegalArgumentException("public key cannot be encoded");

        String format = publicKey.getFormat();
        if (!"X.509".equalsIgnoreCase(format) && !"x509".equalsIgnoreCase(format))
            throw new IllegalArgumentException("public key encoding format is not X.509");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(encoded);
            byte[] digest = md.digest();
            return IoUtils.hexDump(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (this).getClass() != o.getClass()) return false;

        PublicKeyHash that = (PublicKeyHash) o;

        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash != null ? hash.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PublicKeyHash{" +
            "hash='" + hash + '\'' +
            '}';
    }
}
