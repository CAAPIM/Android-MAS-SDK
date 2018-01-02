package com.ca.mas.core.context;

import android.content.Context;

import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.util.KeyUtilsAsymmetric;

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.PublicKey;

public abstract class UniqueIdentifier {

    protected String identifier = "";
    private static String identifierKey;

    /**
     * Generates a set of asymmetric keys in the Android keystore and builds the device identifier off of the public key.
     * Apps built with the same sharedUserId value in AndroidManifest.xml will reuse the same identifier.
     * @param context
     */
    public UniqueIdentifier(Context context) throws
            InvalidAlgorithmParameterException, java.io.IOException, java.security.KeyStoreException, java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException, java.security.cert.CertificateException, java.security.UnrecoverableKeyException {
        identifierKey = getIdentifierKey();
        PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey(identifierKey);
        if (publicKey == null) {
            KeyUtilsAsymmetric.generateRsaPrivateKey(context, 2048, identifierKey,
                    String.format("CN=%s, OU=%s", identifierKey, "com.ca"),
                    false, false, Integer.MAX_VALUE, false);
            publicKey = KeyUtilsAsymmetric.getRsaPublicKey(identifierKey);
        }

        //Convert the public key to a hash string
        byte[] encoded = publicKey.getEncoded();

        //Encode to SHA-256 and then convert to a hex string
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(encoded);
        byte[] mdBytes = md.digest();
        identifier = IoUtils.hexDump(mdBytes);
    }

    @Override
    public String toString() {
        return identifier;
    }

    protected abstract String getIdentifierKey();

}
